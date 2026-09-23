package com.example.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpMethod;
import io.jsonwebtoken.Claims;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;


// @Component: o Spring acha esta classe sozinho e passa a usa-la.
// GlobalFilter: vale para TODAS as rotas do gateway, sem precisar listar uma a uma.
@Component
public class TokenFilter implements GlobalFilter, Ordered {

    private static final List<String> ROTAS_PUBLICAS = List.of(
            "/auth-service/usuarios",
            "/auth-service/usuarios/login",
            "/auth-service/usuarios/refresh"
    );

    private final SecretKey chave;

    public TokenFilter(@Value("${jwt.secret}") String segredo) {
        this.chave = Keys.hmacShaKeyFor(
                segredo.getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain
    ) {
        String caminho = exchange.getRequest().getURI().getPath();

        if (HttpMethod.POST.equals(exchange.getRequest().getMethod())
                && ROTAS_PUBLICAS.contains(caminho)) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authorization == null
                || !authorization.startsWith("Bearer ")
                || authorization.substring(7).isBlank()) {
            return recusar(exchange);
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(chave)
                    .build()
                    .parseSignedClaims(authorization.substring(7))
                    .getPayload();

            if (!"api-vendas".equals(claims.getIssuer())
                    || !"access".equals(
                    claims.get("token_type", String.class)
            )
                    || claims.getSubject() == null
                    || claims.getSubject().isBlank()
                    || claims.getExpiration() == null) {
                return recusar(exchange);
            }
        } catch (Exception e) {
            return recusar(exchange);
        }

        return chain.filter(exchange);
    }

    private Mono<Void> recusar(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }

    /*// As unicas rotas que passam sem token. Sem elas ninguem consegue se
    // cadastrar nem pegar o primeiro token -- o sistema tranca por fora.
    private static final List<String> LIVRES = List.of(
            "/auth-service/usuarios/login",
            "/auth-service/usuarios");

    private final SecretKey chave;

    // @Value pega a chave das configuracoes. hmacShaKeyFor transforma o texto
    // em chave de verdade. E' a MESMA do auth-service: la assina, aqui confere.
    public TokenFilter(@Value("${jwt.secret}") String segredo) {
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
    }

    // Este metodo roda a cada requisicao que chega no gateway.
    // exchange = a requisicao e a resposta. chain = a fila do que vem depois.
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String caminho = exchange.getRequest().getURI().getPath();

        // Rota livre: chain.filter e' o "pode seguir", sem conferir nada.
        if (LIVRES.contains(caminho)) {
            return chain.filter(exchange);
        }

        // Le o cabecalho onde o crachá viaja: "Authorization: Bearer eyJhbGci..."
        String cabecalho = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // Nao mandou cabecalho, ou mandou em outro formato: nem olha o token.
        if (cabecalho == null || !cabecalho.startsWith("Bearer ")) {
            return recusar(exchange);
        }

        try {
            // substring(7) corta o "Bearer " (7 letras) e deixa so' o token.
            // parseSignedClaims confere a assinatura com a nossa chave e
            // estoura excecao se o token for falso ou tiver sido alterado.
            Jwts.parser()
                    .verifyWith(chave)
                    .build()
                    .parseSignedClaims(cabecalho.substring(7));
        } catch (Exception e) {
            return recusar(exchange);
        }

        // Token conferido: a requisicao segue para o servico de destino.
        return chain.filter(exchange);
    }

    // Responde 401 e encerra ali: setComplete fecha a resposta sem chamar o servico.
    private Mono<Void> recusar(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    // A ordem importa: -1 faz este filtro rodar ANTES do roteamento, enquanto
    // o caminho ainda comeca com /auth-service. Depois do roteamento esse
    // prefixo some, a lista LIVRES nao bate mais e o login fica bloqueado.
    @Override
    public int getOrder() {
        return -1;
    }*/

}