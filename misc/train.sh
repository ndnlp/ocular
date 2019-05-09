#!/usr/bin/env bash

#java -Done-jar.main.class=edu.berkeley.cs.nlp.ocular.main.InitializeLanguageModel -mx60g -jar ocular-0.3-SNAPSHOT-with_dependencies.jar -inputTextPath ../text_proc/vol2_proc.txt -outputLmPath lm/0224_2.lmser
java -Done-jar.main.class=edu.berkeley.cs.nlp.ocular.main.InitializeFont -mx60g -jar ocular-0.3-SNAPSHOT-with_dependencies.jar -inputLmPath lm/0224_2.lmser -outputFontPath font/0225_2.fontser -allowedFontsPath fonts.txt
java -Done-jar.main.class=edu.berkeley.cs.nlp.ocular.main.TrainFont -mx30g -jar ocular-0.3-SNAPSHOT-with_dependencies.jar -inputFontPath font/0225_2.fontser -inputLmPath lm/0224_2.lmser -inputDocPath ../186r.jpg -outputFontPath font/0225_2_out.fontser -outputPath 0225_2_output

