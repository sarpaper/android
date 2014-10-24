/*   Sarpaper Android Application
 *   Copyright (C) 2014  Arpa Piemonte
 *
 *   This file is part of Sarpaper Android Application.

 * 	This software is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *   This software is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */package it.piemonte.arpa.sarpaper.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class Ftp {

	// Now, declare a public FTP client object.

	private static final String TAG = "MyFTPClient";
	public FTPClient mFTPClient = null;
	private String uploadFolder;

	public Ftp(String uploadFolder) {
		
		
		
		this.uploadFolder=uploadFolder;
	}
	// Method to connect to FTP server:
	public boolean ftpConnect(String host, String username, String password,
			int port) {
		try {
			mFTPClient = new FTPClient();
			// connecting to the host
			mFTPClient.connect(host, port);

			// now check the reply code, if positive mean connection success
			if (FTPReply.isPositiveCompletion(mFTPClient.getReplyCode())) {
				// login using username & password
				boolean status = mFTPClient.login(username, password);

				
				mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
				mFTPClient.enterLocalPassiveMode();

				return status;
			}
		} catch (Exception e) {
			Log.d(TAG, "Error: could not connect to host " + host);
		}

		return false;
	}

	// Method to disconnect from FTP server:

	public boolean ftpDisconnect() {
		try {
			mFTPClient.logout();
			mFTPClient.disconnect();
			return true;
		} catch (Exception e) {
			Log.d(TAG, "Error occurred while disconnecting from ftp server.");
		}

		return false;
	}

	// Method to get current working directory:

	public String ftpGetCurrentWorkingDirectory() {
		try {
			String workingDir = mFTPClient.printWorkingDirectory();
			return workingDir;
		} catch (Exception e) {
			Log.d(TAG, "Error: could not get current working directory.");
		}

		return null;
	}

	// Method to change working directory:

	public boolean ftpChangeDirectory(String directory_path) {
		try {
			mFTPClient.changeWorkingDirectory(directory_path);
		} catch (Exception e) {
			Log.d(TAG, "Error: could not change directory to " + directory_path);
		}

		return false;
	}

	// Method to list all files in a directory:

	public void ftpPrintFilesList(String dir_path) {
		try {
			FTPFile[] ftpFiles = mFTPClient.listFiles(dir_path);
			int length = ftpFiles.length;

			for (int i = 0; i < length; i++) {
				String name = ftpFiles[i].getName();
				boolean isFile = ftpFiles[i].isFile();

				if (isFile) {
					Log.i(TAG, "File : " + name);
				} else {
					Log.i(TAG, "Directory : " + name);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Method to create new directory:

	public boolean ftpMakeDirectory(String new_dir_path) {
		try {
			boolean status = mFTPClient.makeDirectory(new_dir_path);
			return status;
		} catch (Exception e) {
			Log.d(TAG, "Error: could not create new directory named "
					+ new_dir_path);
		}

		return false;
	}

	// Method to delete/remove a directory:

	public boolean ftpRemoveDirectory(String dir_path) {
		try {
			boolean status = mFTPClient.removeDirectory(dir_path);
			return status;
		} catch (Exception e) {
			Log.d(TAG, "Error: could not remove directory named " + dir_path);
		}

		return false;
	}

	// Method to delete a file:

	public boolean ftpRemoveFile(String filePath) {
		try {
			boolean status = mFTPClient.deleteFile(filePath);
			return status;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	// Method to rename a file:

	public boolean ftpRenameFile(String from, String to) {
		try {
			boolean status = mFTPClient.rename(from, to);
			return status;
		} catch (Exception e) {
			Log.d(TAG, "Could not rename file: " + from + " to: " + to);
		}

		return false;
	}

	
	public boolean ftpDownload(String srcFilePath, String desFilePath) {
		boolean status = false;
		try {
			FileOutputStream desFileStream = new FileOutputStream(desFilePath);
			;
			status = mFTPClient.retrieveFile(srcFilePath, desFileStream);
			desFileStream.close();

			return status;
		} catch (Exception e) {
			Log.d(TAG, "download failed");
		}

		return status;
	}

	@SuppressWarnings("finally")
	public boolean ftpuploadfolder() {
		boolean ok = true;
		File file;
		try {

			String root_sd = Environment.getExternalStorageDirectory()
					.toString() + "/" + uploadFolder + "/toSend/";
			file = new File(root_sd);
			File list[] = file.listFiles();
			if (list != null) {
				for (int i = 0; i < list.length; i++) {
					ftpUploadLog(list[i].getName());
				}
			}
		} catch (Exception e) {
			ok = false;

		} finally {
			return ok;
		}
	}

	
	public boolean ftpUpload(String srcFilePath, String desFileName,
			String desDirectory, Context context) {
		boolean status = false;
		try {
		
			FileInputStream srcFileStream = context.openFileInput(srcFilePath);

		
			status = mFTPClient.storeFile(desFileName, srcFileStream);
		

			srcFileStream.close();
			return status;
		} catch (Exception e) {
			Log.d(TAG, "upload failed: " + e);
		}

		return status;
	}

	public boolean ftpUploadLog(String srcFilePath) {
		boolean status = false;
		try {
			String path = Environment.getExternalStorageDirectory()
					+ "/" + uploadFolder + "/toSend/" + srcFilePath;
			FileInputStream srcFileStream = new FileInputStream(path);
			status = mFTPClient.storeFile(srcFilePath, srcFileStream);
			srcFileStream.close();
			if (status == true) // cancello il file uploaded
			{
				File file = new File(path);
				boolean deleted = file.delete();
			}
			return status;
		} catch (Exception e) {
			Log.d(TAG, "upload failed: " + e);
		}

		return status;
	}
}