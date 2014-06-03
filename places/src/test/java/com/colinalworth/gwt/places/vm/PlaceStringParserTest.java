package com.colinalworth.gwt.places.vm;

import com.colinalworth.gwt.places.vm.PlaceStringModel.PathComponent;
import com.colinalworth.gwt.places.vm.PlaceStringModel.PathConstant;
import com.colinalworth.gwt.places.vm.PlaceStringModel.PathVariable;
import org.junit.Test;

import java.io.StringReader;
import java.util.List;

public class PlaceStringParserTest {
	@Test
	public void testSimpleExpressions() throws ParseException {
		PlaceStringModel model;
		model = parser("");
		assert model != null;
		model = parser("example/{id}/");
		assert model != null;
		model = parser("search/project/?q={query}");
		assert model != null;
		model = parser("project/new/");
		assert model != null;
		model = parser("project/{id}/{activeFile?}/");
		assert model != null;
		model = parser("profile/{id}/edit/");
		assert model != null;
		model = parser("profile/{id}/");
		assert model != null;
		model = parser("search/profile/?q={query?}");
		assert model != null;
		model = parser("?param={path}");
		assert model != null;
	}
	
	@Test
	public void testPath() throws ParseException {
		PlaceStringModel model;
		model = parser("constant/s/");
		List<PathComponent> path = model.getPath();
		assert path.size() == 2;
		assert path.get(0) instanceof PathConstant;
		assert ((PathConstant) path.get(0)).getValue().equals("constant");
		assert path.get(1) instanceof PathConstant;
		assert ((PathConstant) path.get(1)).getValue().equals("s");

		model = parser("{first}/variable/");
		path = model.getPath();
		assert path.size() == 2;
		assert path.get(0) instanceof PathVariable;
		assert !((PathVariable) path.get(0)).isOptional();
		assert ((PathVariable) path.get(0)).getVarName().equals("first");
		assert path.get(1) instanceof PathConstant;
		assert ((PathConstant) path.get(1)).getValue().equals("variable");

		model = parser("second/{variable}/");
		path = model.getPath();
		assert path.size() == 2;
		assert path.get(0) instanceof PathConstant;
		assert ((PathConstant) path.get(0)).getValue().equals("second");
		assert path.get(1) instanceof PathVariable;
		assert !((PathVariable) path.get(1)).isOptional();
		assert ((PathVariable) path.get(1)).getVarName().equals("variable");

		model = parser("{something}/{is?}/{optional}/");
		path = model.getPath();
		assert path.size() == 3;
		assert path.get(0) instanceof PathVariable;
		assert !((PathVariable) path.get(0)).isOptional();
		assert ((PathVariable) path.get(0)).getVarName().equals("something");
		assert path.get(1) instanceof PathVariable;
		assert ((PathVariable) path.get(1)).isOptional();
		assert ((PathVariable) path.get(1)).getVarName().equals("is");
		assert path.get(2) instanceof PathVariable;
		assert !((PathVariable) path.get(2)).isOptional();
		assert ((PathVariable) path.get(2)).getVarName().equals("optional");
	}

	@Test
	public void testFailedExpressions() {
//		assertParseFailure("foo", "missing trailing slash");//TODO remove this requirement
		assertParseFailure("foo{expr}/", "mixed path literal and expression 1");
		assertParseFailure("{expr}expr/", "mixed path literal and expression 2");
		assertParseFailure("?a=b", "non-parameterized query");
	}

	private void assertParseFailure(String str, String message) {
		try {
			parser(str);
			assert false : message;
		} catch (ParseException | TokenMgrError e) {
			//expected
		}
	}

	private PlaceStringModel parser(String str) throws ParseException {
		return new PlaceStringParser(new StringReader(str + "\n")).url();
	}
}
