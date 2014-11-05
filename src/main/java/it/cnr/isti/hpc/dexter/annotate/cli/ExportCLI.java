

/**
 *  Copyright 2014 Salvatore Trani
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package it.cnr.isti.hpc.dexter.annotate.cli;

import it.cnr.isti.hpc.cli.AbstractCommandLineInterface;
import it.cnr.isti.hpc.dexter.annotate.bean.Action;
import it.cnr.isti.hpc.dexter.annotate.bean.AnnotatedSpot;
import it.cnr.isti.hpc.dexter.annotate.bean.AnnotationStatus;
import it.cnr.isti.hpc.dexter.annotate.bean.UserAnnotation;
import it.cnr.isti.hpc.dexter.annotate.dao.SqliteDao;
import it.cnr.isti.hpc.log.ProgressLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * @author Salvatore Trani <salvatore.trani@isti.cnr.it>
 * 
 *         Created on Jun 4, 2014
 */
public class ExportCLI extends AbstractCommandLineInterface {
	private static Gson gson = new Gson();
	private static String[] params = new String[] { "outputDir" };
	private static SqliteDao dao = SqliteDao.getInstance();

	public ExportCLI(String[] args) {
		super(args, params, "java -jar $jar " + ExportCLI.class + " -outputDir dir");
	}

	public static void main(String[] args) throws SQLException {
		ExportCLI cli = new ExportCLI(args);
		String outputDir = cli.getParam("outputDir");

		// Check the outputDir is there
		File dir = new File(outputDir);
		dir.mkdir();
		
		exportUserAnnotations(outputDir);
		exportAnnotationStatus(outputDir);
		exportActions(outputDir);
		exportSpots(outputDir);
	}
	
	private static void exportAnnotationStatus(String outputDir) throws SQLException {
		ProgressLogger pl = new ProgressLogger("exported {} user annotations", 100);
		List<AnnotationStatus> annotationsStatus = dao.getAllUserAnnotations(true);
		File f = new File(outputDir, "annotations-status.json");
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(f);
			for (AnnotationStatus as : annotationsStatus) {
				pl.up();
				writer.println(gson.toJson(as));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (writer != null)
				writer.close();
		}
	}
	
	private static void exportUserAnnotations(String outputDir) throws SQLException {
		ProgressLogger pl = new ProgressLogger("exported {} user annotations", 100);
		List<UserAnnotation> userAnnotations = dao.getUserAnnotations(true);
		File f = new File(outputDir, "user-annotations.json");
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(f);
			for (UserAnnotation userAnnotation : userAnnotations) {
				pl.up();
				writer.println(gson.toJson(userAnnotation));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (writer != null)
				writer.close();
		}
	}
	
	private static void exportActions(String outputDir) throws SQLException {
		ProgressLogger pl = new ProgressLogger("exported {} actions", 100);
		List<Action> actions = dao.getAllActions();
		File f = new File(outputDir, "actions.json");
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(f);
			for (Action action : actions) {
				pl.up();
				writer.println(gson.toJson(action));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (writer != null)
				writer.close();
		}
	}
	
	private static void exportSpots(String outputDir) throws SQLException {
		ProgressLogger pl = new ProgressLogger("exported {} spots", 100);
		List<AnnotatedSpot> spots = dao.getAllSpots();
		File f = new File(outputDir, "spots.json");
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(f);
			for (AnnotatedSpot spot : spots) {
				pl.up();
				writer.println(gson.toJson(spot));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (writer != null)
				writer.close();
		}
	}
	
}