/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.util.FileUtils;

// SleeJar defines common behaviour amongst all the various types of deployable jars.
// Specifically construction of classpaths, properties that define whether the task should
// use deployment descriptor info to deduce what classes should be included etc.
//
// A default name for the deployment descriptor is assumed if one is not provided, which means
// in the common case the 'foo'jarxml property can safely be ommitted. This task also defines
// an extxml property to allow oc extension descriptors to be defined.

public abstract class SleeJar extends org.apache.tools.ant.taskdefs.Jar implements Component {
    public SleeJar(String archiveType, String emptyBehaviour) {
        super();
        this.archiveType = archiveType;
        this.emptyBehavior = emptyBehavior;
        this.jarXmlStr = archiveType + ".xml";
    }

    public void setMetainfbase(String metainfbase) {
        this.metainfbase = new File(metainfbase);
    }

    public void setMetaInfBase(File metainfbase) {
        this.metainfbase = metainfbase;
    }

    // Component interface
    public File getComponentFile(Project project) throws BuildException {
        if (generateName && zipFile == null) {
            // Use an autogenerated name.
            try {
                zipFile = File.createTempFile(getComponentType(), ".jar");
                zipFile.deleteOnExit();

		// Create the ZIP file entry table, else the ANT Jar task
		// grumbles loudly and generates lots of spam warning
		// messages when it tries to open a completely empty file
		// as a ZIP file.

		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));

		// Add an empty directory - ZIP files must have at least one
		// entry.
		ZipEntry ze = new ZipEntry("META-INF/");
		ze.setSize (0);
		ze.setMethod (ZipEntry.STORED);
		// An empty CRC for the directory.
		ze.setCrc(new CRC32().getValue());
		zos.putNextEntry(ze);
		zos.close();

            } catch (IOException ioe) {
                throw new BuildException(ioe);
            }
        }

        return zipFile;
    }

    // Main execution entry point.
    public void execute() throws BuildException {
        long start = System.currentTimeMillis();

        if (autoinclude && super.isInUpdateMode())
            throw new BuildException("update mode not supported when autoinclude=true");

        if (null == jarXmlStr)
            throw new BuildException(getJarXmlName() + " attribute is required", getLocation());

        if (autoinclude) {
            includeClasses();
            autoinclude = false; // On subsequent executions, don't add them again.
        }

        /* (discard) */ getComponentFile(getProject()); // Ensure zipFile is set.
        super.execute();

        long end = System.currentTimeMillis();
    }

    // Setters:
    //   classpath="..."
    public void setClasspath(Path newClasspath) {
        if (this.classpath == null)
            this.classpath = newClasspath;
        else
            this.classpath.append(newClasspath);
    }

    //   nested <classpath>
    public Path createClasspath() {
        if (classpath == null)
            classpath = new Path(getProject());

        return classpath.createPath();
    }

    //   classpathref="..."
    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }

    public void setGeneratename(boolean onoff) {
        this.generateName = onoff;
    }

    //   autoinclude="..."
    public void setAutoinclude(boolean onoff) {
        this.autoinclude = onoff;
    }

    // subclasses implement these operations
    protected abstract void includeTypeSpecificClasses() throws BuildException;
    protected abstract String getComponentType();
    protected abstract String getJarXmlName();

    private void includeClasses() throws BuildException {
        if (classpathFileList == null) {
            // Initialize the classpath once.

            if (classpath == null)
                throw new BuildException("autoinclude set, but classpath attribute not set");
            
            String[] classpathList = classpath.list();
            classpathFileList = new File[classpathList.length];
            classpathZipList = new ZipFile[classpathList.length];
            for (int i = 0; i < classpathList.length; ++i)
                classpathFileList[i] = fileUtils.normalize(classpathList[i]);
        }

        processJarXml();

        includeTypeSpecificClasses();
    }

    protected final void includeClass(String className) throws BuildException {
        String osPath = className.replace('.', File.separatorChar) + ".class";
        String urlPath = className.replace('.', '/') + ".class";

        // Search classpath elements.
        for (int i = 0; i < classpathFileList.length; ++i) {
            File cpBase = classpathFileList[i];
            
            if (!cpBase.exists())
                continue; // Missing.

            if (cpBase.isDirectory()) {
                File testFile = fileUtils.resolveFile(cpBase, osPath);
                if (testFile.exists()) {
                    // Found it as a .class file
                    FileSet fileSet = new FileSet();
                    fileSet.setDir(cpBase);
                    fileSet.setIncludes(osPath);
                    super.addFileset(fileSet);
                    return;
                }

                continue;
            }

            if (cpBase.isFile()) {
                // Assume it's a zip/jarfile.

                if (classpathZipList[i] == null) {
                    // Initialize the zipfile once.

                    try {
                        classpathZipList[i] = new ZipFile(cpBase);
                    } catch (IOException ioe) {
                        // Can't read the zipfile, ignore it.
                        classpathZipList[i] = null;
                        continue;
                    }
                }

                if (classpathZipList[i].getEntry(urlPath) != null) {
                    // Found it within a .zip/.jar
                    ZipFileSet zipFileSet = new ZipFileSet();
                    zipFileSet.setSrc(classpathFileList[i]);
                    zipFileSet.setIncludes(urlPath);
                    super.addZipfileset(zipFileSet);
                    return;
                }

                continue;
            }

            // unknown classpath entry -- ignore it.
        }

        throw new BuildException("Cannot locate class in classpath: " + className);
    }

    // subclasses will define jar specific 'foojarxml' attributes
    protected final void setJarXml(String jarXmlStr) {
        this.jarXmlStr = jarXmlStr;
    }

    public final void setExtjarxml(String extJarXmlStr) {
        this.extJarXmlStr = extJarXmlStr;
    }

    private void processJarXml() {
        // process the normal descriptor
        jarxml = (null == metainfbase) ? new File(jarXmlStr) : new File(metainfbase, jarXmlStr);
        processDescriptor(jarxml, archiveType + ".xml");

        if (null != extJarXmlStr) {
            // process the ext descriptor if we have one
            final File extXml = (null == metainfbase) ? new File(extJarXmlStr) : new File(metainfbase, extJarXmlStr);
            processDescriptor(extXml, extXml.getName());
        }
    }

    private void processDescriptor(File xmlDtor, String xmlDtorName) {
        if (!xmlDtor.exists())
            throw new BuildException("Deployment descriptor: " + xmlDtor + " does not exist.");

        // Add a new fileset for the DD.
        ZipFileSet fs = new ZipFileSet();
        fs.setFile(xmlDtor);
        fs.setFullpath("META-INF/" + xmlDtorName);
        super.addFileset(fs);
    }

    protected void cleanUp() {
        // Clean up zipfiles 
        if (classpathZipList != null) {
            for (int i = 0; i < classpathZipList.length; ++i) {
                if (classpathZipList[i] != null) {
                    try { classpathZipList[i].close(); }
                    catch (IOException e) {}
                }
            }
            
            classpathFileList = null;
            classpathZipList = null;
        }
    }

//    protected final File getMetaInfBase() { return metainfbase; }
    protected final File getJarXml() { return jarxml; }

    private String jarXmlStr;
    private String extJarXmlStr;
    private File metainfbase = null;
    private File jarxml = null;
    private Path classpath;

    private boolean autoinclude = true;
    private boolean generateName;

    private File[] classpathFileList;
    private ZipFile[] classpathZipList;

    protected static final FileUtils fileUtils = FileUtils.newFileUtils();
    protected static final SleeDTDResolver entityResolver = new SleeDTDResolver();
}
