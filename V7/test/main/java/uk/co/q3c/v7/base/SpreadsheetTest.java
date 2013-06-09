/*
 * Copyright (C) 2013 David Sowerby
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.co.q3c.v7.base;

import java.io.File;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.After;
import org.junit.Before;

public abstract class SpreadsheetTest {
	protected OPCPackage pkg;
	protected XSSFWorkbook wb;
	protected XSSFSheet sheet;

	@Before
	public void setup() throws InvalidFormatException, IOException {
		loadSpreadsheet("SitemapLineParserTest_data.xlsx");
	}

	@After
	public void teardown() throws IOException {
		closeSpreadsheet();
	}

	private void loadSpreadsheet(String filename) throws InvalidFormatException, IOException {
		// XSSFWorkbook, File
		File testDataRoot = new File("test/data");
		File f = new File(testDataRoot, filename);
		System.out.println(f.getAbsolutePath());
		pkg = OPCPackage.open(f);
		wb = new XSSFWorkbook(pkg);
		sheet = wb.getSheet("data");

	}

	private void closeSpreadsheet() throws IOException {
		pkg.close();
	}

	/**
	 * Returns empty string instead of null
	 * 
	 * @param i
	 * @param row
	 * @return
	 */
	protected String cellValueNoNull(XSSFRow row, int column) {
		XSSFCell cell = row.getCell(column);
		if (cell == null) {
			return "";
		}
		String s = cell.getStringCellValue();
		if (s == null) {
			return "";
		} else {
			return s;
		}
	}

}
