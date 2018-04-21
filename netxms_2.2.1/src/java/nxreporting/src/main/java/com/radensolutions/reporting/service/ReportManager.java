package com.radensolutions.reporting.service;

import com.radensolutions.reporting.model.ReportDefinition;
import com.radensolutions.reporting.model.ReportResult;
import org.netxms.client.reporting.ReportRenderFormat;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public interface ReportManager {
    List<UUID> listReports();

    ReportDefinition getReportDefinition(UUID reportId, Locale locale);

    void deploy();

    boolean execute(int userId, UUID reportId, UUID jobId, Map<String, String> parameters, Locale locale);

    List<ReportResult> listResults(UUID reportId, int userId);

    boolean deleteResult(UUID reportId, UUID jobId);

    File renderResult(UUID reportId, UUID jobId, ReportRenderFormat format);
}
