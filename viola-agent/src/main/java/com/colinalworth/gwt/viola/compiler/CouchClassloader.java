package com.colinalworth.gwt.viola.compiler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.colinalworth.gwt.viola.entity.SourceProject;
import com.colinalworth.gwt.viola.service.JobService;

public class CouchClassloader extends ClassLoader {
	private final SourceProject source;
	private final JobService service;
	private File tmpDir;
	public CouchClassloader(SourceProject source, JobService service) {
		this.source = source;
		this.service = service;
	}
	
	public void dispose() {
		if (tmpDir != null) {
			tmpDir.delete();
		}
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		if (name.startsWith("project")) {
			System.out.println("cl.findClass " + name);
		}
		return super.findClass(name);
	}
	
	@Override
	protected URL findResource(String name) {
		System.out.println("cl.findResource " + name);
		//verify file exists
		if (!source.getAttachments().containsKey(name)) {
			return null;
		}
		
		//download file, save temp
		if (tmpDir == null) {
			try {
				tmpDir = File.createTempFile("rxf-", "-" + source.getId());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			tmpDir.delete();
			tmpDir.mkdir();
		}
		File newFile = new File(tmpDir, name);
		newFile.getParentFile().mkdirs();
		byte[] buffer = new byte[8 * 1024];
		
		InputStream stream = service.getSourceAsStream(source, name);
		try {
			OutputStream out = new FileOutputStream(newFile);
			try {
				int bytesRead;
				while ((bytesRead = stream.read(buffer)) != -1) {
					out.write(buffer, 0, bytesRead);
				}
			} finally {
				out.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//return url to temp
	
		try {
			return newFile.toURI().toURL();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	

}
