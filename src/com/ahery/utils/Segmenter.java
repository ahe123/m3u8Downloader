package com.ahery.utils;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;

public class Segmenter {
    private static final String basedir = System.getProperty("basedir", "data");
    private static final String MODEL = System.getProperty("model", basedir + "/ctb.gz");
    private static final String DICT = System.getProperty("dict", basedir + "/dict-chris6.ser.gz");

    private CRFClassifier<CoreLabel> segmenter;

    public Segmenter(String[] args) throws IOException {
        System.setOut(new PrintStream(System.out, true, "utf-8"));
        Properties props = new Properties();
        props.setProperty("sighanCorporaDict", basedir);
        // props.setProperty("NormalizationTable", "data/norm.simp.utf8");
        // props.setProperty("normTableEncoding", "UTF-8");
        // below is needed because CTBSegDocumentIteratorFactory accesses it
        props.setProperty("serDictionary", DICT);
        if (args.length > 0) {
            props.setProperty("testFile", args[0]);
        }
        props.setProperty("inputEncoding", "UTF-8");
        props.setProperty("sighanPostProcessing", "true");

        segmenter = new CRFClassifier<>(props);
        segmenter.loadClassifierNoExceptions(MODEL, props);
        for (String filename : args) {
            segmenter.classifyAndWriteAnswers(filename);
        }
    }

    public List<String> segmentOverOneChar(String source){
        List<String> segmented = segmenter.segmentString(source);
        for(int i=segmented.size()-1;i>=0;i--)
            if(segmented.get(i).length()<=1)
                segmented.remove(i);
        return segmented;
    }

    public CRFClassifier<CoreLabel> getSegmenter() {
        return segmenter;
    }
}
