/*******************************************************************************
 * Copyright 2013 Kumar Bibek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    
 * http://www.apache.org/licenses/LICENSE-2.0
 * 	
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.kbeanie.imagechooser.utils;

import java.io.File;

import android.os.Environment;

import com.kbeanie.imagechooser.exceptions.ChooserException;

public class FileUtils {
    /**
     * Returns the path of the folder specified in external storage
     * @param foldername
     * @return
     */
    public static String getDirectory(String foldername) throws ChooserException {
        File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + foldername);
        if (!directory.exists()) {
            if(!directory.mkdirs()){
                throw new ChooserException("Chould not create directory = "+directory);
            }
        }
        return directory.getAbsolutePath();
    }

    public static String getFileExtension(String filename) throws ChooserException {
       try {
           return filename.substring(filename.lastIndexOf(".") + 1);
       } catch (StringIndexOutOfBoundsException e) {
           throw new ChooserException(e);
       }
    }

}
