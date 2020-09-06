package org.processmining.Guido.utils;

import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import static org.processmining.Guido.converters.DPNConverter.getLifecycle;

public class LogEditor {

    public static XLog removePercentageOfResources(final XLog orgLog) {
        XFactory factory = XFactoryRegistry.instance().currentDefault();
        final XLog modLog = factory.createLog();

        for (XTrace oldTrace : orgLog) {
            XTrace newTrace = factory.createTrace(oldTrace.getAttributes());

            for (XEvent event : oldTrace) {
                XAttributeMap attributes = event.getAttributes();

                int randomNum = ThreadLocalRandom.current().nextInt(0, 101);

                if(randomNum <= 33)
                    attributes.remove("org:resource");

                randomNum = ThreadLocalRandom.current().nextInt(0, 101);

//                if(randomNum <= 25)
//                    attributes.remove("time:timestamp");

                newTrace.add(event);
            }

            modLog.add(newTrace);
        }

        return modLog;
    }

    public static void changeBoolean(XAttributeMap attributes, String name) {
        XAttributeBoolean attributeBoolean = ((XAttributeBoolean) attributes.get(name));
        int randomNum = ThreadLocalRandom.current().nextInt(0, 101);
        boolean val = false;
        if(randomNum <= 50)
            return;
        else if(randomNum <= 75)
            val = true;
        attributeBoolean.setValue(val);
    }

    public static void changeString(XAttributeMap attributes, String name) {
        XAttributeLiteral attributeLiteral = ((XAttributeLiteral) attributes.get(name));
        ArrayList<String> vals = new ArrayList<>();
        if(name.equals("license")) {
            vals.add("VALID");
            vals.add("INVALID");
            vals.add("RECENT");
        }
        else if(name.equals("category")) {
            vals.add("MINIVAN");
            vals.add("COMPACT");
            vals.add("SPORT");
        }
        else
            return;

        int randomNum = ThreadLocalRandom.current().nextInt(0, 101);
        String val = vals.get(0);
        if(randomNum <= 66)
            return;
        else if(randomNum <= 77)
            val = vals.get(1);
        else if(randomNum <= 88)
            val = vals.get(2);

        attributeLiteral.setValue(val);
    }

    public static void changeDiscrete(XAttributeMap attributes, String name) {
        XAttributeDiscrete attributeDiscrete = ((XAttributeDiscrete) attributes.get(name));
        int randomNum = ThreadLocalRandom.current().nextInt(0, 101);
        long val = ThreadLocalRandom.current().nextInt(16, 18);
        if(randomNum <= 60)
            return;
        else if(randomNum <= 68)
            val = ThreadLocalRandom.current().nextInt(18, 25);
        else if(randomNum <= 82)
            val = ThreadLocalRandom.current().nextInt(25, 70);

        attributeDiscrete.setValue(val);
    }

    public static XLog renameTraces(final XLog orgLog) {
        XFactory factory = XFactoryRegistry.instance().currentDefault();
        final XLog modLog = factory.createLog();
        int n = 1;

        for (XTrace oldTrace : orgLog) {
            XTrace newTrace = factory.createTrace(oldTrace.getAttributes());
            XAttributeMap traceAttributes = newTrace.getAttributes();
            ((XAttributeLiteral) traceAttributes.get("concept:name")).setValue("Run " + n++);

            newTrace.setAttributes(traceAttributes);

            newTrace.addAll(oldTrace);

            modLog.add(newTrace);
        }

        return modLog;
    }

    public static XLog normalizeLog(final XLog orgLog) {
        XFactory factory = XFactoryRegistry.instance().currentDefault();
        final XLog modLog = factory.createLog();

        for (XTrace oldTrace : orgLog) {
            XTrace newTrace = factory.createTrace(oldTrace.getAttributes());

            for (XEvent event : oldTrace) {
                if(getLifecycle(event).equals("start")) {
                    newTrace.add(event);

                    XEvent clone = (XEvent) event.clone();
                    invertLifecycle(clone);
                    newTrace.add(clone);
                }

            }

            modLog.add(newTrace);
        }

        return modLog;
    }

    public static XLog changeLog(final XLog orgLog) {
        XFactory factory = XFactoryRegistry.instance().currentDefault();
        final XLog modLog = factory.createLog();

        for (XTrace oldTrace : orgLog) {
            XTrace newTrace = factory.createTrace(oldTrace.getAttributes());

            for (XEvent event : oldTrace) {

                XAttributeMap attributes = event.getAttributes();
                String resource = "Alfa";
                int randomNum = ThreadLocalRandom.current().nextInt(0, 3);
                if(randomNum == 1)
                    resource = "Beta";
                else if(randomNum == 2)
                    resource = "Charlie";
                XAttributeLiteral resourceAttr = factory.createAttributeLiteral("org:resource", resource, null);

                attributes.put("org:resource", resourceAttr);

                event.setAttributes(attributes);

                newTrace.add(event);
            }

            modLog.add(newTrace);
        }

        return modLog;
    }

    public static XLog changeLog2(final XLog orgLog) {
        XFactory factory = XFactoryRegistry.instance().currentDefault();
        final XLog modLog = factory.createLog();

        for (XTrace oldTrace : orgLog) {
            XTrace newTrace = factory.createTrace(oldTrace.getAttributes());

            Date firstEventDate = new Date();
            firstEventDate.setTime(firstEventDate.getTime() - ThreadLocalRandom.current().nextLong(1000L*60*60*24*365*20));

            for (XEvent event : oldTrace) {

                XAttributeMap attributes = event.getAttributes();
                XAttributeTimestamp time = (XAttributeTimestamp) attributes.get("time:timestamp");

                long offset = ThreadLocalRandom.current().nextLong(1000L*60*60*4);
                firstEventDate.setTime(firstEventDate.getTime() + offset);

                time.setValueMillis(firstEventDate.getTime());

                event.setAttributes(attributes);

                newTrace.add(event);
            }

            modLog.add(newTrace);
        }

        return modLog;
    }

    public static XLog changeLog3(final XLog orgLog) {
        XFactory factory = XFactoryRegistry.instance().currentDefault();
        final XLog modLog = factory.createLog();

        for(int i = 0; i < 10; i++) {
            for (XTrace oldTrace : orgLog) {
                XTrace newTrace = factory.createTrace(oldTrace.getAttributes());

                for (XEvent event : oldTrace) {

                    XAttributeMap attributes = event.getAttributes();
                    for(XAttribute attribute : attributes.values()) {
                        if(attribute instanceof XAttributeLiteral)
                            changeString(attributes, attribute.getKey());
                        else if(attribute instanceof XAttributeBoolean)
                            changeBoolean(attributes, attribute.getKey());
                        else if(attribute instanceof XAttributeDiscrete)
                            changeDiscrete(attributes, attribute.getKey());
                    }

                    event.setAttributes(attributes);

                    newTrace.add(event);
                }

                modLog.add(newTrace);
            }
        }

        return modLog;
    }

    public static XLog changeLog4(final XLog orgLog) {
        XFactory factory = XFactoryRegistry.instance().currentDefault();
        final XLog modLog = factory.createLog();

        for (XTrace oldTrace : orgLog) {
            XTrace newTrace = factory.createTrace(oldTrace.getAttributes());

            for (XEvent event : oldTrace) {

                XAttributeMap attributes = event.getAttributes();
                String classifier =  "concept:name";

                XAttribute oldAttribute = attributes.get(classifier);
                XAttribute newAttribute = factory.createAttributeLiteral(classifier, attributes.get("Activity").toString(),
                        oldAttribute.getExtension());

                attributes.put(classifier, newAttribute);
                event.setAttributes(attributes);
                attributes.remove("Activity");

                XEvent clone = (XEvent) event.clone();
                invertLifecycle(clone);
                newTrace.add(clone);

                newTrace.add(event);
            }

            modLog.add(newTrace);
        }

        return modLog;
    }

    public static XLog filterStart(final XLog orgLog) {
        XFactory factory = XFactoryRegistry.instance().currentDefault();
        final XLog modLog = factory.createLog();

        for (XTrace oldTrace : orgLog) {
            XTrace newTrace = factory.createTrace(oldTrace.getAttributes());

            for (XEvent event : oldTrace) {
                if(!getLifecycle(event).equals("start"))
                    newTrace.add(event);
            }

            modLog.add(newTrace);
        }

        return modLog;
    }

    private static void invertLifecycle(XEvent event) {
        switch (getLifecycle(event)) {
            case "start" :
                setLifecycle(event, "complete");
                break;
            case "complete" :
                setLifecycle(event, "start");
                break;
            default :
                throw new IllegalStateException("Unsupported lifecycle transition: " + getLifecycle(event));
        }
    }

    private static void setLifecycle(XEvent event, String lifecycle) {
        XAttributeMap attributes = event.getAttributes();
        XFactory factory = XFactoryRegistry.instance().currentDefault();
        XAttribute attribute = factory.createAttributeLiteral("lifecycle:transition", lifecycle, XLifecycleExtension.instance());
        attributes.put("lifecycle:transition", attribute);
        event.setAttributes(attributes);
    }
}
