#!/bin/sh

. ./set_build_number.sh
#version=2.0-RC2-$build_number
version=2.2.1

cd win32.win32.x86
zip -r nxmc-$version-win32-x86.zip nxmc
mv nxmc-$version-win32-x86.zip ..
cd ..

cd win32.win32.x86_64
zip -r nxmc-$version-win32-x64.zip nxmc
mv nxmc-$version-win32-x64.zip ..
cd ..

cd linux.gtk.x86
tar cvf nxmc-$version-linux-gtk-x86.tar nxmc
gzip nxmc-$version-linux-gtk-x86.tar
mv nxmc-$version-linux-gtk-x86.tar.gz ..
cd ..

cd linux.gtk.x86_64
tar cvf nxmc-$version-linux-gtk-x64.tar nxmc
gzip nxmc-$version-linux-gtk-x64.tar
mv nxmc-$version-linux-gtk-x64.tar.gz ..
cd ..

cd macosx.cocoa.x86_64
tar cvf nxmc-$version-macosx-cocoa-x64.tar nxmc
gzip nxmc-$version-macosx-cocoa-x64.tar
mv nxmc-$version-macosx-cocoa-x64.tar.gz ..
cd ..
