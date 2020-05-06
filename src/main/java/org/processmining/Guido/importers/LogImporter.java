package org.processmining.Guido.importers;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.in.XMxmlParser;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.models.EventLogArray;
import org.processmining.plugins.log.XContextMonitoredInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.GZIPInputStream;

public class LogImporter {
    private static String resourcePath = "src/main/resources/";

//    public static Collection<XLog> process(String name) throws Exception {
//        File file = new File(String.valueOf(ClassLoader.getSystemResource(name)));
//        XUniversalParser universalParser = new XUniversalParser();
//        Collection<XLog> result = universalParser.parse(file);
//        return result;
//    }

//    protected static EventLogArray importLog(PluginContext context, String filename)
//            throws Exception {
//        File file = new File(resourcePath+ filename);
//        InputStream input = new FileInputStream(file);
//        EventLogArray logs = EventLogArrayFactory.createEventLogArray();
//        String parent =  null;
//        logs.importFromStream(context, input, parent);
//        setLabel(context, logs, filename);
//        return logs;
//    }

    public static XLog importLog (PluginContext context, File file) throws Exception {
        InputStream stream;
        if (file.getName().endsWith(".zip")) {
            // Open zip file.
            ZipFile zipFile = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            ZipEntry zipEntry = entries.nextElement();
            if (entries.hasMoreElements()) {
                throw new InvalidParameterException("Zipped log files should not contain more than one entry.");
            }

            // Return stream of only entry in zip file.
            // Do not yet close zip file, as the retruend stream still needs to be read.
            stream = zipFile.getInputStream(zipEntry);
        }
        else if (file.getName().endsWith(".gz") ) {
            stream = new GZIPInputStream(new FileInputStream(file));
        }
        else {
            stream = new FileInputStream(file);
        }
        return importFromStream(context, stream, file.getName(), stream.available(), XFactoryRegistry.instance().currentDefault());
    }

    public static XLog importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes,
                                        XFactory factory) throws Exception {
        //	System.out.println("Open file");
        XParser parser;
        /*
         * Only use MXML parser if the file has th eproper extesnion.
         * In all other cases, use the XES parser.
         */
        if (filename.toLowerCase().endsWith(".mxml") || filename.toLowerCase().endsWith(".mxml.gz")) {
            parser = new XMxmlParser(factory);
        } else {
            parser = new XesXmlParser(factory);
        }
        Collection<XLog> logs = null;
        Exception firstException = null;
        String errorMessage = "";
        try {
            logs = parser.parse(new XContextMonitoredInputStream(input, fileSizeInBytes, context.getProgress()));
        } catch (Exception e) {
            logs = null;
            firstException = e;
            errorMessage = errorMessage + e;
        }
//		if (logs == null || logs.isEmpty()) {
//			// try any other parser
//			for (XParser p : XParserRegistry.instance().getAvailable()) {
//				if (p == parser) {
//					continue;
//				}
//				try {
//					logs = p.parse(new XContextMonitoredInputStream(input, fileSizeInBytes, context.getProgress()));
//					if (logs.size() > 0) {
//						break;
//					}
//				} catch (Exception e1) {
//					// ignore and move on.
//					logs = null;
//					errorMessage = errorMessage + " [" + p.name() + ":" + e1 + "]";
//				}
//			}
//		}

        // Log file has been read from the stream. The zip file (if present) can now be closed.

        // log sanity checks;
        // notify user if the log is awkward / does miss crucial information
        if (logs == null) {
            //			context.getFutureResult(0).cancel(false);
            throw new Exception("Could not open log file, possible cause: "
                    /* + errorMessage, */ + firstException);
        }
        if (logs.size() == 0) {
            //			context.getFutureResult(0).cancel(false);
            throw new Exception("No processes contained in log!");
        }

        XLog log = logs.iterator().next();
        if (XConceptExtension.instance().extractName(log) == null) {
            /*
             * Log name not set. Create a default log name.
             */
            XConceptExtension.instance().assignName(log, "Anonymous log imported from " + filename);
        }

        return log;

    }


    private static void setLabel(PluginContext context, EventLogArray logs, String filename) {
        String prefix = null;
        String postfix = null;
        boolean allSame = true;
        for (int i = 0; i < logs.getSize(); i++) {
            XLog log = logs.getLog(i);
            String name = XConceptExtension.instance().extractName(log);
            if (name != null) {
                if (prefix == null) {
                    prefix = name;
                } else {
                    if (!name.equals(prefix)) {
                        allSame = false;
                        prefix = greatestCommonPrefix(prefix, name);
                    }
                }
                if (postfix == null) {
                    postfix = name;
                } else {
                    if (!name.equals(postfix)) {
                        allSame = false;
                        postfix = new StringBuilder(greatestCommonPrefix(
                                new StringBuilder(prefix).reverse().toString(), new StringBuilder(name).reverse()
                                        .toString())).reverse().toString();
                    }
                }
            }
        }
        if ((prefix != null && prefix.length() > 0) || (postfix != null && postfix.length() > 0)) {
            StringBuffer buf = new StringBuffer();
            if (prefix != null) {
                buf.append(prefix);
            }
            if (!allSame) {
                buf.append(" ... ");
                if (postfix != null) {
                    buf.append(postfix);
                }
            }
            context.getFutureResult(0).setLabel(buf.toString());
        } else {
            context.getFutureResult(0).setLabel("Event log array from file '" + filename + "'");
        }
    }

    private static String greatestCommonPrefix(String a, String b) {
        int minLength = Math.min(a.length(), b.length());
        for (int i = 0; i < minLength; i++) {
            if (a.charAt(i) != b.charAt(i)) {
                return a.substring(0, i);
            }
        }
        return a.substring(0, minLength);
    }

}
