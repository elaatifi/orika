package ma.glasnost.orika.test.generator;

import java.util.Map;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeBuilder;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

public class BeanToMapGenerationTestCase {

	@Test
	public void testBeanToMapGeneration() throws Exception {
		
		
		MapperFactory factory = MappingUtil.getMapperFactory(true);
	
		factory.classMap(Student.class, Map.class)
				.field("grade.letter", "letterGrade")
				.field("grade.point", "GPA")
				.field("grade.percentage", "gradePercentage")
				.field("name.first", "firstName")
				.field("name.last", "lastName")
				.byDefault()
				.register();
		
		MapperFacade mapper = factory.getMapperFacade();
		
		Student student = new Student();
		student.id = "1";
		student.email = "test@test.com";
		student.name = new Name();
		student.name.first = "Chuck";
		student.name.last = "Testa";
		student.grade = new Grade();
		student.grade.letter = "B-";
		student.grade.percentage = 81.5;
		student.grade.point = 2.7;
		
		
		@SuppressWarnings("unchecked")
        Map<String,Object> result = mapper.map(student, Map.class);
		
		Assert.assertEquals(student.id, result.get("id"));
		Assert.assertEquals(student.email, result.get("email"));
		Assert.assertEquals(student.name.first, result.get("firstName"));
		Assert.assertEquals(student.name.last, result.get("lastName"));
		Assert.assertEquals(student.grade.letter, result.get("letterGrade"));
		Assert.assertEquals(student.grade.percentage, result.get("gradePercentage"));
		Assert.assertEquals(student.grade.point, result.get("GPA"));
		
		Student mapBack = mapper.map(result, Student.class);
		
		Assert.assertEquals(student.id, mapBack.id);
        Assert.assertEquals(student.email, mapBack.email);
        Assert.assertEquals(student.name.first, mapBack.name.first);
        Assert.assertEquals(student.name.last, mapBack.name.last);
        Assert.assertEquals(student.grade.letter, mapBack.grade.letter);
        Assert.assertEquals(student.grade.percentage, mapBack.grade.percentage);
        Assert.assertEquals(student.grade.point, mapBack.grade.point);
		
	}
	
	@Test
    public void testBeanToCustomMapGeneration() throws Exception {
        
        
        MapperFactory factory = MappingUtil.getMapperFactory(true);
        
        Type<Map<String, String>> mapType = new TypeBuilder<Map<String, String>>(){}.build();
        Type<Student> studentType = TypeFactory.valueOf(Student.class);
        
        factory.classMap(Student.class, mapType)
                .field("grade.letter", "letterGrade")
                .field("grade.point", "GPA")
                .field("grade.percentage", "gradePercentage")
                .field("name.first", "firstName")
                .field("name.last", "lastName")
                .byDefault()
                .register();
        
        MapperFacade mapper = factory.getMapperFacade();
        
        Student student = new Student();
        student.id = "1";
        student.email = "test@test.com";
        student.name = new Name();
        student.name.first = "Chuck";
        student.name.last = "Testa";
        student.grade = new Grade();
        student.grade.letter = "B-";
        student.grade.percentage = 81.5;
        student.grade.point = 2.7;
        
        Map<String, String> result = mapper.map(student, studentType, mapType);
        
        Assert.assertEquals(student.id, result.get("id"));
        Assert.assertEquals(student.email, result.get("email"));
        Assert.assertEquals(student.name.first, result.get("firstName"));
        Assert.assertEquals(student.name.last, result.get("lastName"));
        Assert.assertEquals(student.grade.letter, result.get("letterGrade"));
        Assert.assertEquals("" + student.grade.percentage, result.get("gradePercentage"));
        Assert.assertEquals("" + student.grade.point, result.get("GPA"));
        
        Student mapBack = mapper.map(result, mapType, studentType);
        
        Assert.assertEquals(student.id, mapBack.id);
        Assert.assertEquals(student.email, mapBack.email);
        Assert.assertEquals(student.name.first, mapBack.name.first);
        Assert.assertEquals(student.name.last, mapBack.name.last);
        Assert.assertEquals(student.grade.letter, mapBack.grade.letter);
        Assert.assertEquals(student.grade.percentage, mapBack.grade.percentage);
        Assert.assertEquals(student.grade.point, mapBack.grade.point);
        
    }
	
	public static class Student {
	    public Grade grade;
	    public String id;
	    public String email;
	    public Name name;
	}
	
	public static class Name {
	    public String first;
	    public String last;
	}
	
	public static class Grade {
		public double point;
		public double percentage;
		public String letter;
	}

}
