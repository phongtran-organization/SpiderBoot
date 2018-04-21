package com.radensolutions.reporting.service.impl;

import com.radensolutions.reporting.ThreadLocalReportInfo;
import com.radensolutions.reporting.model.Notification;
import com.radensolutions.reporting.model.ReportDefinition;
import com.radensolutions.reporting.model.ReportParameter;
import com.radensolutions.reporting.model.ReportResult;
import com.radensolutions.reporting.service.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.query.QueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRLoader;
import org.netxms.client.SessionNotification;
import org.netxms.client.reporting.ReportRenderFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

@SuppressWarnings("deprecation")
@Service("reportManager")
public class FileSystemReportManager implements ReportManager
//        , ApplicationContextAware
{
    private static final String SUBREPORT_DIR_KEY = "SUBREPORT_DIR";
    private static final String USER_ID_KEY = "SYS_USER_ID";
    private static final String DEFINITIONS_DIRECTORY = "definitions";
    private static final String FILE_SUFIX_DEFINITION = ".jrxml";
    private static final String FILE_SUFIX_COMPILED = ".jasper";
    private static final String MAIN_REPORT_COMPILED = "main" + FILE_SUFIX_COMPILED;
    private static final String FILE_SUFIX_FILLED = ".jrprint";

    private static final Logger log = LoggerFactory.getLogger(FileSystemReportManager.class);

    @Value("#{serverSettings.workspace}")
    private String workspace;

    @Autowired
    private Session session;

    @Autowired
    private ReportResultService reportResultService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private SmtpSender smtpSender;

    private Map<UUID, String> reportMap = new HashMap<UUID, String>();

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public List<UUID> listReports() {
        ArrayList<UUID> li = new ArrayList<UUID>(reportMap.keySet());
        System.out.println(li);
        return li;
    }

    @Override
    public ReportDefinition getReportDefinition(UUID reportId, Locale locale) {
        ReportDefinition definition = null;

        final JasperReport jasperReport = loadReport(reportId);
        if (jasperReport != null) {
            final File reportDirectory = getReportDirectory(reportId);
            ResourceBundle labels = loadReportTranslation(reportDirectory, locale);

            definition = new ReportDefinition();
            definition.setName(jasperReport.getName());
            final int numberOfColumns = getPropertyFromMap(jasperReport.getPropertiesMap(), "numberOfColumns", 1);
            definition.setNumberOfColumns(numberOfColumns);

            final JRParameter[] parameters = jasperReport.getParameters();
            int index = 0;
            for (JRParameter jrParameter : parameters) {
                if (!jrParameter.isSystemDefined() && jrParameter.isForPrompting()) {
                    final JRPropertiesMap propertiesMap = jrParameter.getPropertiesMap();
                    final String type = jrParameter.getValueClass().getName();
                    String logicalType = getPropertyFromMap(propertiesMap, "logicalType", type);
                    index = getPropertyFromMap(propertiesMap, "index", index);
                    final JRExpression defaultValue = jrParameter.getDefaultValueExpression();
                    String dependsOn = getPropertyFromMap(propertiesMap, "dependsOn", "");
                    int span = getPropertyFromMap(propertiesMap, "span", 1);
                    if (span < 1) {
                        span = 1;
                    }

                    final ReportParameter parameter = new ReportParameter();
                    final String name = jrParameter.getName();
                    parameter.setName(name);
                    parameter.setType(logicalType);
                    parameter.setIndex(index);
                    parameter.setDescription(getTranslation(labels, name));
                    parameter.setDefaultValue(defaultValue == null ? null : defaultValue.getText());
                    parameter.setDependsOn(dependsOn);
                    parameter.setSpan(span);
                    definition.putParameter(parameter);
                    index++;
                }
            }
        }
        return definition;
    }

    private JasperReport loadReport(UUID uuid) {
        JasperReport jasperReport = null;
        final File reportDirectory = getReportDirectory(uuid);
        final File reportFile = new File(reportDirectory, MAIN_REPORT_COMPILED);
        try {
            jasperReport = (JasperReport) JRLoader.loadObject(reportFile);
        } catch (JRException e) {
            log.error("Can't load compiled report", e);
        }
        return jasperReport;
    }

    @Override
    public void deploy() {
        File definitionsDirectory = getDefinitionsDirectory();
        log.info("Deploying *.jar/*.zip in " + definitionsDirectory.getAbsolutePath());
        String[] files = definitionsDirectory.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String normalizedName = name.toLowerCase();
                return normalizedName.endsWith(".jar") || normalizedName.endsWith(".zip");
            }
        });
        if (files != null) {
            for (String archiveName : files) {
                log.debug("Deploying " + archiveName);
                try {
                    String deployedName = archiveName.split("\\.(?=[^\\.]+$)")[0];
                    File destination = new File(definitionsDirectory, deployedName);
                    deleteFolder(destination);
                    UUID bundleId = unpackJar(destination, new File(definitionsDirectory, archiveName));
                    reportMap.put(bundleId, deployedName);
                    compileReport(destination);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            log.info("No files found");
        }
    }

    private void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    private UUID unpackJar(File destination, File archive) throws IOException {
        JarFile jarFile = new JarFile(archive);
        try {
            Manifest manifest = jarFile.getManifest();
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (jarEntry.isDirectory()) {
                    File destDir = new File(destination, jarEntry.getName());
                    if (!destDir.mkdirs()) {
                        log.error("Can't create directory " + destDir.getAbsolutePath());
                    }
                }
            }

            entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                File destinationFile = new File(destination, jarEntry.getName());

                InputStream inputStream = null;
                FileOutputStream outputStream = null;
                if (jarEntry.isDirectory()) {
                    continue;
                }
                try {
                    inputStream = jarFile.getInputStream(jarEntry);
                    outputStream = new FileOutputStream(destinationFile);

                    byte[] buffer = new byte[1024];
                    int len;
                    assert inputStream != null;
                    while ((len = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, len);
                    }
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                }
            }

            // TODO: handle possible exception
            return UUID.fromString(manifest.getMainAttributes().getValue("Build-Id"));
        } finally {
            jarFile.close();
        }
    }

    private void compileReport(File reportDirectory) {
        final String[] list = reportDirectory.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(FILE_SUFIX_DEFINITION);
            }
        });
        for (String fileName : list) {
            final File file = new File(reportDirectory, fileName);
            try {
                final String sourceFileName = file.getAbsolutePath();
                final String destinationFileName = sourceFileName
                        .substring(0, sourceFileName.length() - FILE_SUFIX_DEFINITION.length()) + FILE_SUFIX_COMPILED;
                JasperCompileManager.compileReportToFile(sourceFileName, destinationFileName);
            } catch (JRException e) {
                log.error("Can't compile report " + file.getAbsoluteFile(), e);
            }
        }
    }

    private File getDefinitionsDirectory() {
        return new File(workspace, DEFINITIONS_DIRECTORY);
    }

    private File getReportDirectory(UUID reportId) {
        String path = reportMap.get(reportId);
        File file = null;
        if (path != null) {
            file = new File(getDefinitionsDirectory(), path);
        }
        return file;
    }

    private File getOutputDirectory(UUID reportId) {
        final File output = new File(workspace, "output");
        if (!output.exists()) {
            output.mkdirs();
        }
        final File file = new File(output, reportId.toString());
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    @Override
    public boolean execute(int userId, UUID reportId, UUID jobId, final Map<String, String> parameters, Locale locale) {
        boolean ret = false;

        DataSource dataSource = null;
        final JasperReport report = loadReport(reportId);
        if (report != null) {
            String name = report.getPropertiesMap().getProperty("datasource");
            if (name != null) {
                dataSource = (DataSource) applicationContext.getBean(name);
            }
        }
        if (dataSource == null) {
            dataSource = (DataSource) applicationContext.getBean(ServerSettings.DC_ID_REPORTING);
        }

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            ret = realExecute(report, userId, reportId, jobId, parameters, locale, connection);
        } catch (SQLException e) {
            log.error("Can't get report connection", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // ignore
                }
            }
        }
        return ret;
    }

    private boolean realExecute(JasperReport report, int userId, UUID reportId, UUID jobId, Map<String, String> parameters, Locale locale,
                                Connection connection) {
        boolean ret = false;
        if (connection != null && report != null) {
            final File reportDirectory = getReportDirectory(reportId);

            final ResourceBundle translations = loadReportTranslation(reportDirectory, locale);

            // fill report parameters
            final HashMap<String, Object> localParameters = new HashMap<String, Object>(parameters);
            localParameters.put(JRParameter.REPORT_LOCALE, locale);
            localParameters.put(JRParameter.REPORT_RESOURCE_BUNDLE, translations);
            String subrepoDirectory = reportDirectory.getPath() + File.separatorChar;
            localParameters.put(SUBREPORT_DIR_KEY, subrepoDirectory);
            localParameters.put(USER_ID_KEY, userId);

            localParameters.put(JRParameter.REPORT_CLASS_LOADER, new URLClassLoader(new URL[]{}, getClass().getClassLoader()));

            prepareParameters(parameters, report, localParameters);

            ThreadLocalReportInfo.setReportLocation(subrepoDirectory);

            final File outputDirectory = getOutputDirectory(reportId);
            final String outputFile = new File(outputDirectory, jobId.toString() + ".jrprint").getPath();
            try {
                DefaultJasperReportsContext reportsContext = DefaultJasperReportsContext.getInstance();
                reportsContext.setProperty(QueryExecuterFactory.QUERY_EXECUTER_FACTORY_PREFIX + "nxcl", "com.radensolutions.reporting.custom.NXCLQueryExecutorFactory");
                JasperFillManager manager = JasperFillManager.getInstance(reportsContext);
                manager.fillToFile(report, outputFile, localParameters, connection);
                reportResultService.save(new ReportResult(new Date(), reportId, jobId, userId));

                ret = true;

                session.sendNotify(SessionNotification.RS_RESULTS_MODIFIED, 0);
                List<Notification> notify = notificationService.load(jobId);
                sendMailNotification(notify);
            } catch (JRException e) {
                log.error("Can't execute report", e);
            }
        }
        return ret;
    }

    private void prepareParameters(Map<String, String> parameters, JasperReport report, HashMap<String, Object> localParameters) {
        final JRParameter[] jrParameters = report.getParameters();
        for (JRParameter jrParameter : jrParameters) {
            if (!jrParameter.isForPrompting() || jrParameter.isSystemDefined()) {
                continue;
            }

            final String jrName = jrParameter.getName();
            final String input = parameters.get(jrName);
            final Class<?> valueClass = jrParameter.getValueClass();

            String logicalType = jrParameter.getPropertiesMap().getProperty("logicalType");

            if ("START_DATE".equalsIgnoreCase(logicalType)) {
                Date date = DateParameterParser.getDateTime(input, false);
                localParameters.put(jrName, date.getTime() / 1000);
            } else if ("END_DATE".equalsIgnoreCase(logicalType)) {
                Date date = DateParameterParser.getDateTime(input, true);
                localParameters.put(jrName, date.getTime() / 1000);
            } else if ("SEVERITY_LIST".equalsIgnoreCase(logicalType)) {
                localParameters.put(jrName, convertCommaSeparatedToIntList(input));
            } else if ("OBJECT_ID_LIST".equalsIgnoreCase(logicalType)) {
                localParameters.put(jrName, convertCommaSeparatedToIntList(input));
            } else if ("EVENT_CODE".equalsIgnoreCase(logicalType)) {
                if ("0".equals(input)) {
                    localParameters.put(jrName, new ArrayList<Integer>(0));
                } else { // not "<any>"
                    localParameters.put(jrName, convertCommaSeparatedToIntList(input));
                }
            } else if (Boolean.class.equals(valueClass)) {
                localParameters.put(jrName, Boolean.parseBoolean(input));
            } else if (Date.class.equals(valueClass)) {
                long timestamp = 0;
                try {
                    timestamp = Long.parseLong(input);
                } catch (NumberFormatException e) {
                    // ignore?
                }
                localParameters.put(jrName, new Date(timestamp));
            } else if (Double.class.equals(valueClass)) {
                double value = 0.0;
                try {
                    value = Double.parseDouble(input);
                } catch (NumberFormatException e) {
                    // ignore?
                }
                localParameters.put(jrName, value);
            } else if (Float.class.equals(valueClass)) {
                float value = 0.0f;
                try {
                    value = Float.parseFloat(input);
                } catch (NumberFormatException e) {
                    // ignore?
                }
                localParameters.put(jrName, value);
            } else if (Integer.class.equals(valueClass)) {
                int value = 0;
                try {
                    value = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    // ignore?
                }
                localParameters.put(jrName, value);
            } else if (Long.class.equals(valueClass)) {
                long value = 0L;
                try {
                    value = Long.parseLong(input);
                } catch (NumberFormatException e) {
                    // ignore?
                }
                localParameters.put(jrName, value);
            } else if (Short.class.equals(valueClass)) {
                short value = 0;
                try {
                    value = Short.parseShort(input);
                } catch (NumberFormatException e) {
                    // ignore?
                }
                localParameters.put(jrName, value);
            } else if (BigDecimal.class.equals(valueClass)) {
                BigDecimal value = BigDecimal.valueOf(0);
                try {
                    value = new BigDecimal(input);
                } catch (NumberFormatException e) {
                    // ignore?
                }
                localParameters.put(jrName, value);
            } else if (Collection.class.equals(valueClass) || List.class.equals(valueClass)) {
                final List<String> list = Arrays.asList(input.trim().split("\\t"));
                localParameters.put(jrName, list);
            }
        }
    }

    private List<Integer> convertCommaSeparatedToIntList(String input) {
        if (input == null) {
            return null;
        }

        String[] strings = input.split(",");
        List<Integer> ret = new ArrayList<Integer>(strings.length);
        for (String s : strings) {
            try {
                ret.add(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                // TODO: handle
                log.error("Invalid ID in comma separated list: " + input);
            }
        }
        return ret;
    }

    @Override
    public List<ReportResult> listResults(UUID reportId, int userId) {
        return reportResultService.list(reportId, userId);
    }

    @Override
    public boolean deleteResult(UUID reportId, UUID jobId) {
        reportResultService.delete(jobId);
        final File reportDirectory = getOutputDirectory(reportId);
        final File file = new File(reportDirectory, jobId.toString() + FILE_SUFIX_FILLED);
        return file.delete();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public File renderResult(UUID reportId, UUID jobId, ReportRenderFormat format) {
        final File outputDirectory = getOutputDirectory(reportId);
        final File dataFile = new File(outputDirectory, jobId.toString() + ".jrprint");

        File outputFile = new File(outputDirectory, jobId.toString() + "." + System.currentTimeMillis() + ".render");

        JRAbstractExporter exporter = null;
        switch (format) {
            case PDF:
                exporter = new JRPdfExporter();
                break;
            case XLS:
                exporter = new JRXlsExporter();
                final JasperReport jasperReport = loadReport(reportId);
                if (jasperReport != null) {
                    exporter.setParameter(JRXlsExporterParameter.SHEET_NAMES,
                            new String[]{prepareXlsSheetName(jasperReport.getName())});
                }
                exporter.setParameter(JRXlsExporterParameter.IS_IGNORE_CELL_BORDER, false);
                exporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, false);
                // Arrange cell spacing and remove paging gaps
                exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, true);
                exporter.setParameter(JRXlsExporterParameter.IS_COLLAPSE_ROW_SPAN, true);
                exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS, true);
                exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, false);
                exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, true);
                // Arrange graphics
                exporter.setParameter(JRXlsExporterParameter.IS_IMAGE_BORDER_FIX_ENABLED, true);
                exporter.setParameter(JRXlsExporterParameter.IS_FONT_SIZE_FIX_ENABLED, true);
                exporter.setParameter(JRXlsExporterParameter.IS_IGNORE_GRAPHICS, false);
                break;
            default:
                break;
        }

        exporter.setParameter(JRExporterParameter.INPUT_FILE, dataFile);

        try {
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, new FileOutputStream(outputFile));
            exporter.exportReport();
        } catch (Exception e) {
            log.error("Failed to render report", e);
            outputFile.delete();
            outputFile = null;
        }

        return outputFile;
    }

    /**
     * Prepare Excel sheet name. Excel sheet name doesn't contain special chars: / \ ? * ] [ Maximum sheet name length is 31 and
     * sheet names must not begin or end with ' (apostrophe)
     *
     * @param sheetName proposed sheet name
     * @return valid sheet name
     */
    private String prepareXlsSheetName(String sheetName) {
        int length = Math.min(31, sheetName.length());
        StringBuilder stringBuilder = new StringBuilder(sheetName.substring(0, length));
        for (int i = 0; i < length; i++) {
            char ch = stringBuilder.charAt(i);
            switch (ch) {
                case '/':
                case '\\':
                case '?':
                case '*':
                case ']':
                case '[':
                    stringBuilder.setCharAt(i, ' ');
                    break;
                case '\'':
                    if (i == 0 || i == length - 1)
                        stringBuilder.setCharAt(i, ' ');
                    break;
                default:
                    break;
            }
        }
        return stringBuilder.toString();
    }

    private String getTranslation(ResourceBundle bundle, String name) {
        if (bundle.containsKey(name)) {
            return bundle.getString(name);
        }
        return name;
    }

    private int getPropertyFromMap(JRPropertiesMap map, String key, int defaultValue) {
        if (map.containsProperty(key)) {
            try {
                return Integer.parseInt(map.getProperty(key));
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private String getPropertyFromMap(JRPropertiesMap map, String key, String defaultValue) {
        if (map.containsProperty(key)) {
            return map.getProperty(key);
        }
        return defaultValue;
    }

    private ResourceBundle loadReportTranslation(File directory, Locale locale) {
        ResourceBundle labels = null;
        try {
            final URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{directory.toURI().toURL()});
            labels = ResourceBundle.getBundle("i18n", locale, classLoader);
        } catch (Exception e) {
            log.error("Can't load language bundle for report", e);
            // on error create empty bundle
            labels = new ListResourceBundle() {
                @Override
                protected Object[][] getContents() {
                    return new Object[0][];
                }
            };
        }
        return labels;
    }

    /**
     * Send an email notification
     */
    private void sendMailNotification(List<Notification> notify) {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date today = Calendar.getInstance().getTime();
        String reportDate = df.format(today);

        for (Notification notification : notify) {
            String text = String.format("Report \"%s\" successfully generated on %s and can be accessed using management console.",
                    notification.getReportName(), reportDate);

            String fileName = null;
            File renderResult = null;
            if (notification.getAttachFormatCode() != 0) {
                ReportRenderFormat formatCode = ReportRenderFormat.valueOf(notification.getAttachFormatCode());
                UUID jobId = notification.getJobId();
                UUID reportId = reportResultService.findReportId(jobId);
                if (reportId != null) {
                    renderResult = renderResult(reportId, jobId, formatCode);
                    String time = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
                    fileName = String.format("%s %s.%s", notification.getReportName(), time,
                            formatCode == ReportRenderFormat.PDF ? "pdf" : "xls");
                    text += "\n\nPlease find attached copy of the report.";
                } else {
                    log.error("Cannot find report by guid");
                }
            }
            text += "\n\nThis message is generated automatically by NetXMS.";

            if (renderResult != null) {
                smtpSender.mail(notification.getMail(), "New report is available", text, fileName, renderResult);
                renderResult.delete();
            }
        }
    }

//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        this.applicationContext = applicationContext;
//    }
}
