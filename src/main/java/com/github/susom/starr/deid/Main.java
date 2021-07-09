/*
 * Copyright 2019 The Board of Trustees of The Leland Stanford Junior University.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package com.github.susom.starr.deid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation;
import org.apache.beam.sdk.options.ValueProvider;
import org.apache.beam.sdk.options.ValueProvider.NestedValueProvider;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.TupleTagList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import com.google.gson.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.Gson;

/**
 * Deid Pipeline.
 * @author wenchengl
 */
public class Main implements Serializable {
  private static final Logger log = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws IOException {
    new Main().run(args);
  }

  private void run(String[] args) throws IOException {

    DeidOptions options = getOptions(args);

    log.info(options.toString());

    DeidJobs jobs = null;
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    try {

      File deidConfigFileHd = new File(options.getDeidConfigFile());
      if (deidConfigFileHd.exists()) {
        log.info("reading configuration from file " + deidConfigFileHd.getAbsolutePath());
        jobs = mapper.readValue(deidConfigFileHd, DeidJobs.class);
      } else {
        log.info("reading configuration from " + options.getDeidConfigFile());
        jobs = mapper.readValue(this.getClass().getClassLoader()
          .getResourceAsStream(options.getDeidConfigFile()), DeidJobs.class);
      }
      log.info("received configuration for " + jobs.name);

    } catch (IOException e) {
      log.error("Could not open or parse deid configuration file in class path",e);
      System.exit(1);
    }
/*
    if (options.getGoogleDlpEnabled() != null) {
      log.info("overwriting GoogleDlpEnabled property with {}", options.getGoogleDlpEnabled());
      boolean enableDlp = options.getGoogleDlpEnabled().toLowerCase(Locale.ROOT).equals("true");
      for (int i = 0; i < jobs.deidJobs.length;i++) {
        jobs.deidJobs[i].googleDlpEnabled = enableDlp;
      }
    }
*/
    if (options.getTextIdFields() != null) {
      //override text field mapping
      for (int i = 0; i < jobs.deidJobs.length;i++) {
        if (options.getTextIdFields() != null) {
          jobs.deidJobs[i].textIdFields = options.getTextIdFields();
        }
        if (options.getTextInputFields() != null) {
          jobs.deidJobs[i].textFields = options.getTextInputFields();
        }
      }
    }

    log.info("input file type is " + options.getInputFileType().get());
    String input = options.getInputResource().get();

    File input1 = new File(input);
 // rahul starts
    File output1 = new File(options.getOutputResource().get());
	try(Stream<Path> walk = Files.walk(Paths.get(output1.getAbsolutePath()))){
    	walk.filter(Files::isRegularFile)
    	.map(Path::toFile)
    	.forEach(File::delete);
    } catch (IOException e) {
    	e.printStackTrace();
    }
	Path path = Paths.get(input1.getAbsolutePath());
	Gson gson = new Gson();
	List<String> csvjsonStrings = new ArrayList<String>();
	List<FileHeaders> fileHeaders = new ArrayList<FileHeaders>();
	


	if (input1.isFile()) {
		// read file contents
		FileInputStream fstream;
		try {
			fstream = new FileInputStream(path.toString());
			InputStreamReader is = new InputStreamReader(fstream, "UTF-8");
			populateList(gson,csvjsonStrings,path,is);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	List<Path> fileResult;
	if (input1.isDirectory()) {
		try (Stream<Path> walk = Files.walk(path)) {
			fileResult = walk.filter(Files::isRegularFile).collect(Collectors.toList());
		}
		fileResult.parallelStream().forEach(fl -> {
			FileInputStream fstream;
			try {
				fstream = new FileInputStream(fl.toString());
				InputStreamReader is = new InputStreamReader(fstream, "UTF-8");
				populateList(gson,csvjsonStrings,fl,is);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}
 
//    CsvSchema csvSchema = CsvSchema.builder().setUseHeader(true).build();
//    CsvMapper csvMapper = new CsvMapper();
//    List<Object> readAll = csvMapper.readerFor(Map.class).with(csvSchema).readValues(input1).readAll();
//    List<String> csvjsonStrings = readAll.stream()
//    .map(obj ->new Gson().toJson(obj))
//    .collect(Collectors.toList());
//     rahul ends  
   
    //start the work
    Pipeline p = Pipeline.create(options);
    PCollectionTuple result =
        (options.getInputType().equals(ResourceType.gcp_bq.name())
          ? InputTransforms.BigQueryRowToJson.withBigQueryLink(p, options.getInputResource())
          //: p.apply(jsonFormattedDataList)
          : p.apply(Create.of(csvjsonStrings)).setCoder(StringUtf8Coder.of()))
        .apply("Deid", new DeidTransform(jobs.deidJobs[0], options.getDlpProject()))
        .apply("processResult",
          ParDo.of(new DeidResultProc(jobs.deidJobs[0].analytic))
            .withOutputTags(DeidTransform.fullResultTag,
                TupleTagList.of(DeidTransform.statsDlpPhiTypeTag)
                .and(DeidTransform.statsPhiTypeTag)
                .and(DeidTransform.statPhiFoundByTag)));

    if (jobs.deidJobs[0].analytic) {

      if (jobs.deidJobs[0].googleDlpEnabled) {
        result.get(DeidTransform.statsDlpPhiTypeTag)
            .apply("AnalyzeCategoryStatsDlp", new AnalyzeStatsTransform())
            .apply(MapElements.via(new ProcessAnalytics.PrintCounts()))
            .apply(TextIO.write().to(
              NestedValueProvider.of(options.getOutputResource(),
                new AppendSuffixSerializableFunction("/statsDlpPhiType")))
          );
      }

      result.get(DeidTransform.statsPhiTypeTag)
          .apply("AnalyzeGlobalStage2", new AnalyzeStatsTransform())
          .apply(MapElements.via(new ProcessAnalytics.PrintCounts()))
          .apply(TextIO.write().to(
            NestedValueProvider.of(options.getOutputResource(),
                new AppendSuffixSerializableFunction("/statsPhiTypeStage2")))
        );

      result.get(DeidTransform.statPhiFoundByTag)
          .apply("AnalyzeFoundbyStats", new AnalyzeStatsTransform())
          .apply(MapElements.via(new ProcessAnalytics.PrintCounts()))
          .apply(TextIO.write().to(
            NestedValueProvider.of(options.getOutputResource(),
                new AppendSuffixSerializableFunction("/statPhiFoundBy")))
        );
    }

    result.get(DeidTransform.fullResultTag)
        .apply(TextIO.write().to(
          NestedValueProvider.of(options.getOutputResource(),
              new AppendSuffixSerializableFunction("/DeidNote")))
      );

    PipelineResult pipelineResult = p.run();

    if (!isTemplateCreationRun(args)) {
      pipelineResult.waitUntilFinish();
    }
    //rahul starts
  
    if (output1.isDirectory()) {
    	try (Stream<Path> walk = Files.walk(Paths.get(output1.getAbsolutePath()))) {
    		fileResult = walk.filter(Files::isRegularFile).collect(Collectors.toList());
    	}
    	fileResult.parallelStream().forEach(fl -> {
    		try (FileInputStream fstream = new FileInputStream(fl.toString())){
    			InputStreamReader is = new InputStreamReader(fstream, "UTF-8");
    			populateListOfFileHeaders(is,fileHeaders,fl);
    		} catch (FileNotFoundException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (UnsupportedEncodingException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (IOException e1) {
    			// TODO Auto-generated catch block
    			e1.printStackTrace();
    		} 

    	});
    }    
    try(Stream<Path> walk = Files.walk(Paths.get(output1.getAbsolutePath()))){
    	walk.filter(Files::isRegularFile)
    	.map(Path::toFile)
    	.forEach(File::delete);
    } catch (IOException e) {
    	e.printStackTrace();
    }
    fileHeaders.removeIf(Objects::isNull);
    fileHeaders.parallelStream().forEach(f -> {
    	File fil = new File(output1.getAbsolutePath()+File.separator+f.getId());
    	try {
    		fil.createNewFile();
    		FileWriter fileWriter = new FileWriter(fil);
    		PrintWriter printWriter = new PrintWriter(fileWriter);
    		printWriter.print(f.getTranscription());
    		printWriter.close();
    	} catch (IOException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}

    });
  }

private void populateList(Gson gson, List<String> csvjsonStrings, Path path,InputStreamReader is) {
	  // TODO Auto-generated method stub	  
	  String jsonInString = gson.toJson(new FileHeaders(null, new BufferedReader(is)
			  .lines().collect(Collectors.joining("\n")),null,null,path.getFileName().toString()));
	  csvjsonStrings.add(jsonInString);
}


private void populateListOfFileHeaders(InputStreamReader is, List<FileHeaders> fileheaders, Path fl) {
	// TODO Auto-generated method stub
	Gson gson = new Gson();
	String collect = new BufferedReader(is).lines().collect(Collectors.joining("\n"));
	List<String> split =  Arrays.asList(collect.split("\\r?\\n"));
	split.stream().forEach(sp ->{
		FileHeaders fromJson = gson.fromJson(sp, FileHeaders.class);
		fileheaders.add(fromJson);
	});

}
// rahul ends

  private static boolean isTemplateCreationRun(String[] args) {
    for (String arg : args) {
      if (arg.contains("--templateLocation")) {
        return true;
      }
    }
    return false;
  }

  private static DeidOptions getOptions(String[] args) throws IOException {
    DeidOptions options = PipelineOptionsFactory.fromArgs(args)
        .withValidation().as(DeidOptions.class);

//    if (options.getGcpCredentialsKeyFile().trim().length() > 0) {
//      GoogleCredentials credentials =
//          GoogleCredentials.fromStream(new FileInputStream(options.getGcpCredentialsKeyFile()))
//          .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));
//      options.setGcpCredential(credentials);
//    }

    return options;
  }

  private static class AppendSuffixSerializableFunction
      implements SerializableFunction<String, String> {
    private String suffix;

    public AppendSuffixSerializableFunction(String suffix) {
      this.suffix = suffix;
    }

    @Override
    public String apply(String prefix) {
      return prefix + suffix;
    }
  }

  public interface DeidOptions extends PipelineOptions {
    @Description("Set GCP credentials key file if using DataflowRunner.")
    @Default.String("")
    String getGcpCredentialsKeyFile();

    void setGcpCredentialsKeyFile(String value);

    //void setGcpCredential(Credentials value);

    @Description("Namen of the Deid configuration, the default is deid_test_config.yaml")
    @Default.String("deid_config_clarity.yaml")
    String getDeidConfigFile();

    void setDeidConfigFile(String value);

    @Description("input file type, optional")
    @Default.String("JSON")
    ValueProvider<String> getInputFileType();

    void setInputFileType(ValueProvider<String> value);

    @Description("Path of the file to read from")
    @Validation.Required
    ValueProvider<String> getInputResource();

    void setInputResource(ValueProvider<String> value);

    @Description("Path of the file to save to")
    @Validation.Required
    ValueProvider<String> getOutputResource();

    void setOutputResource(ValueProvider<String> value);

    @Description("type of the input resouce. gcp_gcs | gcp_bq | local")
    @Default.String("gcp_gcs")
    String getInputType();

    void setInputType(String value);

    @Description("The Google project id that DLP API will be called, optional.")
    @Default.String("")
    String getDlpProject();

    void setDlpProject(String value);

    @Description("Turn on/off Google DLP")
    String getGoogleDlpEnabled();

    void setGoogleDlpEnabled(String value);

    @Description("override text field, optional.")
    ValueProvider<String> getTextInputFields();

    void setTextInputFields(ValueProvider<String> value);

    @Description("override text-id field, optional.")
    ValueProvider<String> getTextIdFields();

    void setTextIdFields(ValueProvider<String> value);
  }

}
