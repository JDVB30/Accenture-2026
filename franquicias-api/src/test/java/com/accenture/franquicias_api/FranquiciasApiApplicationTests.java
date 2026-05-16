package com.accenture.franquicias_api;

import com.accenture.franquicias_api.dto.FranquiciaDto.*;
import com.accenture.franquicias_api.model.Franquicia;
import com.accenture.franquicias_api.repository.FranquiciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

// Spring Boot 4.0: @AutoConfigureWebTestClient fue eliminado.
// WebTestClient se inyecta automáticamente con RANDOM_PORT en contexto reactivo.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FranquiciasApiApplicationTests {

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private FranquiciaRepository franquiciaRepository;

	@BeforeEach
	void setUp() {
		franquiciaRepository.deleteAll().block();
	}

	// ─── Tests de Franquicia ──────────────────────────────────────────────────

	@Test
	@DisplayName("Debe crear una franquicia exitosamente")
	void crearFranquicia_debeRetornar201() {
		FranquiciaRequest request = new FranquiciaRequest("Franquicia Test");

		// Spring Boot 4.0 WebFlux: contentType() va ANTES de bodyValue()
		webTestClient.post()
				.uri("/api/franquicias")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(request)
				.exchange()
				.expectStatus().isCreated()
				.expectBody(Franquicia.class)
				.value(franquicia -> {
					assertThat(franquicia.getId()).isNotNull();
					assertThat(franquicia.getNombre()).isEqualTo("Franquicia Test");
					assertThat(franquicia.getSucursales()).isEmpty();
				});
	}

	@Test
	@DisplayName("Debe fallar al crear franquicia sin nombre")
	void crearFranquicia_sinNombre_debeRetornar400() {
		FranquiciaRequest request = new FranquiciaRequest("");

		webTestClient.post()
				.uri("/api/franquicias")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(request)
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	@DisplayName("Debe actualizar el nombre de una franquicia")
	void actualizarNombreFranquicia_debeRetornar200() {
		Franquicia franquicia = franquiciaRepository.save(
				Franquicia.builder().id("f1").nombre("Original").build()
		).block();

		webTestClient.put()
				.uri("/api/franquicias/{id}/nombre", franquicia.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(new NombreRequest("Nombre Actualizado"))
				.exchange()
				.expectStatus().isOk()
				.expectBody(Franquicia.class)
				.value(f -> assertThat(f.getNombre()).isEqualTo("Nombre Actualizado"));
	}

	// ─── Tests de Sucursal ────────────────────────────────────────────────────

	@Test
	@DisplayName("Debe agregar una sucursal a una franquicia")
	void agregarSucursal_debeRetornar201() {
		Franquicia franquicia = franquiciaRepository.save(
				Franquicia.builder().id("f1").nombre("Franquicia A").build()
		).block();

		webTestClient.post()
				.uri("/api/franquicias/{id}/sucursales", franquicia.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(new SucursalRequest("Sucursal Norte"))
				.exchange()
				.expectStatus().isCreated()
				.expectBody(Franquicia.class)
				.value(f -> {
					assertThat(f.getSucursales()).hasSize(1);
					assertThat(f.getSucursales().get(0).getNombre()).isEqualTo("Sucursal Norte");
				});
	}

	@Test
	@DisplayName("Debe retornar 404 al agregar sucursal a franquicia inexistente")
	void agregarSucursal_franquiciaNoExiste_debeRetornar404() {
		webTestClient.post()
				.uri("/api/franquicias/no-existe/sucursales")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(new SucursalRequest("Sucursal X"))
				.exchange()
				.expectStatus().isNotFound();
	}

	// ─── Tests de Producto ────────────────────────────────────────────────────

	@Test
	@DisplayName("Debe agregar un producto a una sucursal y actualizar su stock")
	void agregarProductoYActualizarStock_flujoCompleto() {
		Franquicia franquicia = franquiciaRepository.save(
				Franquicia.builder().id("f1").nombre("Franquicia A").build()
		).block();

		// 2. Agregar sucursal
		Franquicia conSucursal = webTestClient.post()
				.uri("/api/franquicias/{id}/sucursales", franquicia.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(new SucursalRequest("Sucursal Sur"))
				.exchange()
				.returnResult(Franquicia.class)
				.getResponseBody().blockFirst();

		String sucursalId = conSucursal.getSucursales().get(0).getId();

		// 3. Agregar producto
		Franquicia conProducto = webTestClient.post()
				.uri("/api/franquicias/{fId}/sucursales/{sId}/productos",
						franquicia.getId(), sucursalId)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(new ProductoRequest("Producto Z", 50))
				.exchange()
				.returnResult(Franquicia.class)
				.getResponseBody().blockFirst();

		String productoId = conProducto.getSucursales().get(0).getProductos().get(0).getId();
		assertThat(conProducto.getSucursales().get(0).getProductos().get(0).getStock()).isEqualTo(50);

		// 4. Actualizar stock
		webTestClient.put()
				.uri("/api/franquicias/{fId}/sucursales/{sId}/productos/{pId}/stock",
						franquicia.getId(), sucursalId, productoId)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(new StockRequest(200))
				.exchange()
				.expectStatus().isOk()
				.expectBody(Franquicia.class)
				.value(f -> assertThat(
						f.getSucursales().get(0).getProductos().get(0).getStock()
				).isEqualTo(200));
	}

	// ─── Test consulta mayor stock ─────────────────────────────────────────────

	@Test
	@DisplayName("Debe retornar el producto con mayor stock por sucursal")
	void obtenerProductoMayorStock_debeRetornarUnoPorSucursal() {
		Franquicia f = franquiciaRepository.save(
				Franquicia.builder().id("f1").nombre("Franquicia Q").build()
		).block();

		Franquicia f1 = webTestClient.post()
				.uri("/api/franquicias/{id}/sucursales", f.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(new SucursalRequest("Sucursal A"))
				.exchange()
				.returnResult(Franquicia.class)
				.getResponseBody().blockFirst();

		String s1Id = f1.getSucursales().get(0).getId();

		webTestClient.post()
				.uri("/api/franquicias/{fId}/sucursales/{sId}/productos", f.getId(), s1Id)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(new ProductoRequest("Prod A", 10))
				.exchange()
				.expectStatus().isCreated();

		webTestClient.post()
				.uri("/api/franquicias/{fId}/sucursales/{sId}/productos", f.getId(), s1Id)
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(new ProductoRequest("Prod B", 99))
				.exchange()
				.expectStatus().isCreated();

		webTestClient.get()
				.uri("/api/franquicias/{id}/producto-mayor-stock", f.getId())
				.exchange()
				.expectStatus().isOk()
				.expectBodyList(ProductoMayorStockResponse.class)
				.value(list -> {
					assertThat(list).hasSize(1);
					assertThat(list.get(0).getProductoNombre()).isEqualTo("Prod B");
					assertThat(list.get(0).getStock()).isEqualTo(99);
				});
	}
}
