DRIVER = db2

pkglib_LTLIBRARIES = db2.la
db2_la_SOURCES = db2.cpp
db2_la_CPPFLAGS=-I@top_srcdir@/include -I@top_srcdir@/src/server/include @DB2_CPPFLAGS@
db2_la_LDFLAGS = -module -avoid-version @DB2_LDFLAGS@
db2_la_LIBADD = ../../../libnetxms/libnetxms.la @DB2_LIBS@

EXTRA_DIST = db2.vcxproj db2.vcxproj.filters db2drv.h

install-exec-hook:
	if test "x`uname -s`" = "xAIX" ; then OBJECT_MODE=@OBJECT_MODE@ $(AR) x $(DESTDIR)$(pkglibdir)/$(DRIVER).a $(DESTDIR)$(pkglibdir)/$(DRIVER).so ; rm -f $(DESTDIR)$(pkglibdir)/$(DRIVER).a ; fi
	mkdir -p $(DESTDIR)$(pkglibdir)/dbdrv
	mv -f $(DESTDIR)$(pkglibdir)/$(DRIVER).so $(DESTDIR)$(pkglibdir)/dbdrv/$(DRIVER).ddr
	rm -f $(DESTDIR)$(pkglibdir)/$(DRIVER).la	
	rm -f $(DESTDIR)$(libdir)/libnxddr_$(DRIVER).so*
	ln -s netxms/dbdrv/$(DRIVER).ddr $(DESTDIR)$(libdir)/libnxddr_$(DRIVER).so
