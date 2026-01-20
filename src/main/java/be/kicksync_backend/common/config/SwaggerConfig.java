package be.kicksync_backend.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Configuration
public class SwaggerConfig {

    private static final List<Tag> TAGS = List.of(
            new Tag().name("User").description("사용자 인증 및 관리 (회원가입/로그인)"),
            new Tag().name("Partner Auth").description("입점사 인증 (회원가입)"),
            new Tag().name("Partner Product").description("입점사 상품 관리 (등록/수정/삭제)"),
            new Tag().name("Product").description("상품 조회 (사용자)"),
            new Tag().name("Order").description("주문 관리 (사용자)"),
            new Tag().name("Payment").description("결제 관리"),
            new Tag().name("MyPage").description("마이페이지 (프로필/주문내역)"),
            new Tag().name("Token").description("토큰 관리 API"),
            new Tag().name("Admin Product").description("관리자 상품 관리")
    );

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("kicksync-api")
                .pathsToMatch("/api/**")
                .addOpenApiCustomizer(openApi -> {
                    // 태그 순서 정의
                    Map<String, Integer> order = IntStream.range(0, TAGS.size())
                            .boxed()
                            .collect(Collectors.toMap(i -> TAGS.get(i).getName(), i -> i));

                    // 현재 OpenAPI에 있는 태그 가져오기 (없으면 초기화)
                    List<Tag> currentTags = openApi.getTags();
                    if (currentTags == null) {
                        currentTags = new ArrayList<>();
                    }

                    // 정의된 태그가 없으면 추가 (설명 포함)
                    for (Tag tag : TAGS) {
                        if (currentTags.stream().noneMatch(t -> t.getName().equals(tag.getName()))) {
                            currentTags.add(tag);
                        }
                    }

                    currentTags.sort(Comparator.comparingInt(tag -> order.getOrDefault(tag.getName(), 999)));
                    openApi.setTags(currentTags);
                })
                .build();
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("KickSync API")
                        .description("KickSync Application API Documentation")
                        .version("v1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()))
                .tags(new ArrayList<>(TAGS)); // 초기 태그 설정
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }
}
