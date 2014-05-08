/*
 * Copyright 2014 Diamond Light Source Ltd. and iSencia Belgium NV.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dawnsci.passerelle.cluster.service.drmaa.internal;

import java.io.File;

public class ClusterServiceConfigurer {

  private static final String DEFAULT_SUBFOLDER_PREFIX = "auto";
  private final static String PROCESSING_FOLDER_PROPNAME = "org.dawnsci.hpc.processing.folder";
  private final static String JOB_FOLDER_PROPNAME = "org.dawnsci.hpc.job.folder";
  private final static String DEFAULT_PROCESSING_FOLDER = "processing";
  private final static String DEFAULT_JOB_FOLDER = "job";

  private String processingFolderName;
  private String jobFolderName;

  public ClusterServiceConfigurer() {
    processingFolderName = System.getProperty(PROCESSING_FOLDER_PROPNAME, DEFAULT_PROCESSING_FOLDER);
    jobFolderName = System.getProperty(JOB_FOLDER_PROPNAME, DEFAULT_JOB_FOLDER);
  }

  /**
   * TODO check if/how we need to support disparate platforms for control workflow and cluster nodes.
   * E.g. if the paths are to be set in a control workflow on windows, but need to identify files on a linux filesystem???
   * 
   * @param collectedDataFile
   * @param processingIdentifier a key that identifies a given processing run on the collected data. If null, default is
   *          "auto".
   * @return the root folder for processing results for the given file with data collected from an experiment
   */
  public File getProcessingRootForCollectedData(File collectedDataFile, String processingIdentifier) {
    File dataFolder = collectedDataFile.getParentFile();
    String baseName = getFileNameNoExtension(collectedDataFile);
    String prefix = processingIdentifier != null ? processingIdentifier : DEFAULT_SUBFOLDER_PREFIX;

    String folderName = processingFolderName + File.separatorChar + prefix + "_" + baseName;
    return new File(dataFolder, folderName);
  }

  /**
   * 
   * @param processingRootFolder
   * @param jobIdentifier typically a numerical counter, but could be a string ID or so as well
   * @return
   */
  public File getNewProcessingJobFolder(File processingRootFolder) {
    File sug = new File(processingRootFolder, jobFolderName + "_1");
    if (sug.exists()) {
      int i = 2;
      while (sug.exists()) {
        sug = new File(processingRootFolder, jobFolderName + "_" + i);
        ++i;
      }
    }
    sug.mkdirs();
    return sug;
  }

  /**
   * Get Filename minus it's extension if present
   * 
   * @param file File to get filename from
   * @return String filename minus its extension
   */
  private static String getFileNameNoExtension(File file) {
    final String fileName = file.getName();
    int posExt = fileName.lastIndexOf(".");
    // No File Extension
    return posExt == -1 ? fileName : fileName.substring(0, posExt);
  }
}
