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
package uk.co.q3c.v7.base.navigate;

import static org.fest.assertions.Assertions.*;
import static org.mockito.Mockito.*;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import uk.co.q3c.v7.base.SpreadsheetTest;

import com.mycila.testing.junit.MycilaJunitRunner;
import com.mycila.testing.plugin.guice.GuiceContext;

@RunWith(MycilaJunitRunner.class)
@GuiceContext({})
public class SitemapLineParserTest extends SpreadsheetTest {

	@Mock
	Sitemap sitemap;

	@Test
	public void spreadsheet_data() {

		for (int r = 1; r <= 20; r++) {
			XSSFRow row = sheet.getRow(r);

			// input
			String testLine = row.getCell(0).getStringCellValue();

			// outputs
			int level = (int) row.getCell(1).getNumericCellValue();
			String segment = cellValueNoNull(row, 2);
			String view = row.getCell(3).getStringCellValue();
			String key = row.getCell(4).getStringCellValue();
			int errors = (int) row.getCell(5).getNumericCellValue();

			// when
			SitemapLineParser slp = new SitemapLineParser(sitemap, testLine, 1);

			// then
			System.out.println("testing spreadsheet row " + (r + 1) + " with input " + testLine);
			assertThat(slp.level()).overridingErrorMessage("level at row  " + r).isEqualTo(level);
			assertThat(slp.segment()).overridingErrorMessage("segment at row  " + r).isEqualTo(segment);
			// assertThat(slp.view()).overridingErrorMessage("view at row  " + r).isEqualTo(view);
			assertThat(slp.view()).isEqualTo(view);
			assertThat(slp.key()).overridingErrorMessage("key at row  " + r).isEqualTo(key);
			assertThat(slp.errorCount()).overridingErrorMessage("errors at row  " + r).isEqualTo(errors);

		}

	}

	@Test
	public void noindent() {

		// given
		String testLine = "segment  :SegmentView ~ Seg";
		// when
		SitemapLineParser slp = new SitemapLineParser(sitemap, testLine, 1);
		// then
		assertThat(slp.errorCount()).isEqualTo(1);
		assertThat(slp.isMissingHyphen()).isTrue();
		verify(sitemap).error();

	}

}
