# Copyright (C) 2006 NetXMS Team <bugs@netxms.org>
#  
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

SUBAGENT = hpux

pkglib_LTLIBRARIES = hpux.la
hpux_la_SOURCES = disk.cpp iostat.cpp main.cpp net.cpp proc.cpp system.cpp
hpux_la_CPPFLAGS = -I@top_srcdir@/include -D_PSTAT64
hpux_la_LIBADD = ../../libnxagent/libnxagent.la ../../../libnetxms/libnetxms.la -lnm
hpux_la_LDFLAGS = -module -avoid-version -export-symbols ../platform-subagent.sym

EXTRA_DIST = disk.h hpux.h net.h system.h

if !STATIC_BUILD
install-exec-hook:
	mv -f $(DESTDIR)$(pkglibdir)/$(SUBAGENT)@SHLIB_SUFFIX@ $(DESTDIR)$(pkglibdir)/$(SUBAGENT).nsm
	rm -f $(DESTDIR)$(pkglibdir)/$(SUBAGENT).la
	rm -f $(DESTDIR)$(libdir)/libnsm_$(SUBAGENT)@SHLIB_SUFFIX@
	ln -s netxms/$(SUBAGENT).nsm $(DESTDIR)$(libdir)/libnsm_$(SUBAGENT)@SHLIB_SUFFIX@
endif
