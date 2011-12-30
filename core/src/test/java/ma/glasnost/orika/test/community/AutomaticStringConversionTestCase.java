package ma.glasnost.orika.test.community;

import java.io.Serializable;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;

import org.junit.Assert;
import org.junit.Test;

public class AutomaticStringConversionTestCase {

	public static class CamposEntrada implements Serializable {

		private static final long serialVersionUID = -1751914753847603413L;

		private int numero;
		private String prefijo;
		private String orden;
		private int id;

		public int getNumero() {
			return numero;
		}

		public void setNumero(int numero) {
			this.numero = numero;
		}

		public String getPrefijo() {
			return prefijo;
		}

		public void setPrefijo(String prefijo) {
			this.prefijo = prefijo;
		}

		public String getOrden() {
			return orden;
		}

		public void setOrden(String orden) {
			this.orden = orden;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

	}

	public static class CamposSalida implements Serializable {

		private static final long serialVersionUID = -1775063854225489603L;

		private String numero;
		private int prefijo;
		private String orden;
		private int id;

		public String getNumero() {
			return numero;
		}

		public void setNumero(String numero) {
			this.numero = numero;
		}

		public int getPrefijo() {
			return prefijo;
		}

		public void setPrefijo(int prefijo) {
			this.prefijo = prefijo;
		}

		public String getOrden() {
			return orden;
		}

		public void setOrden(String orden) {
			this.orden = orden;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

	}

	@Test
	public void testMapCamposEntradaToCamposSalida() throws Exception {

		CamposEntrada source = new CamposEntrada();
		source.setId(5);
		source.setNumero(77);
		source.setOrden("ASC");
		source.setPrefijo("2");
		
		MapperFactory factory = new DefaultMapperFactory.Builder().build();
		factory.registerClassMap(ClassMapBuilder
				.map(CamposEntrada.class, CamposSalida.class).byDefault()
				.toClassMap());
		factory.build();
		MapperFacade facade = factory.getMapperFacade();
		
		CamposSalida result = facade.map(source, CamposSalida.class);
		
		Assert.assertEquals(result.getId(), source.getId());
		Assert.assertEquals(result.getNumero(), ""+source.getNumero());
		Assert.assertEquals(result.getOrden(), source.getOrden());
		Assert.assertEquals(""+result.getPrefijo(), source.getPrefijo());
	}
}
