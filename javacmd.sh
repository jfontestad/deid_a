#!/bin/sh

java -jar /opt/deid.jar --deidConfigFile=deid_config_omop_genrep.yaml --textIdFields="id" --textInputFields="transcription" --inputFileType="txt" --inputResource=/opt/sample_notes --outputResource=local_withOut_phi_result_1

/bin/bash