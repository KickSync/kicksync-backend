package be.kicksync_backend.feature.order.controller;

import be.kicksync_backend.common.dto.ApiResponse;
import be.kicksync_backend.common.dto.ResponseText;
import be.kicksync_backend.common.security.UserDetailsImpl;
import be.kicksync_backend.feature.order.dto.OrderCancelRequestDto;
import be.kicksync_backend.feature.order.dto.OrderCreateRequestDto;
import be.kicksync_backend.feature.order.dto.OrderResponseDto;
import be.kicksync_backend.feature.order.entity.Order;
import be.kicksync_backend.feature.order.service.OrderFacade;
import be.kicksync_backend.feature.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderFacade orderFacade;

    /**
     * 주문 생성 API
     *
     * @param requestDto  주문 생성 요청 데이터
     * @param userDetails 인증된 사용자 정보
     * @return 생성된 주문 정보
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponseDto>> createOrder(
            @Valid @RequestBody OrderCreateRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        OrderResponseDto responseDto = orderFacade.createOrderWithLock(requestDto, userDetails.getUser().getId());
        ApiResponse<OrderResponseDto> apiResponse = ApiResponse.<OrderResponseDto>builder()
                .msg(ResponseText.ORDER_CREATE_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.CREATED.value()))
                .data(responseDto)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    /**
     * 주문 상세 조회 API
     *
     * @param orderId     주문 ID
     * @param userDetails 인증된 사용자 정보
     * @return 주문 상세 정보
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrderDetails(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Order order = orderService.getOrderDetails(orderId, userDetails.getUser().getId());
        OrderResponseDto responseDto = new OrderResponseDto(order);
        ApiResponse<OrderResponseDto> apiResponse = ApiResponse.<OrderResponseDto>builder()
                .msg(ResponseText.ORDER_DETAIL_FETCH_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .data(responseDto)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * 사용자 주문 목록 조회 API
     *
     * @param userDetails 인증된 사용자 정보
     * @return 사용자의 주문 목록
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getUserOrders(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<Order> orders = orderService.getUserOrders(userDetails.getUser().getId());
        List<OrderResponseDto> responseDtos = orders.stream()
                .map(OrderResponseDto::from)
                .collect(Collectors.toList());

        ApiResponse<List<OrderResponseDto>> apiResponse = ApiResponse.<List<OrderResponseDto>>builder()
                .msg(ResponseText.ORDER_LIST_FETCH_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .data(responseDtos)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * 주문 취소 API
     *
     * @param orderId     취소할 주문 ID
     * @param requestDto  취소 요청 데이터
     * @param userDetails 인증된 사용자 정보
     * @return 취소 완료 메시지
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable Long orderId,
            @RequestBody(required = false) OrderCancelRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        orderService.cancelOrder(orderId, requestDto, userDetails.getUser().getId());
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .msg(ResponseText.ORDER_CANCEL_SUCCESS.getMsg())
                .statuscode(String.valueOf(HttpStatus.OK.value()))
                .build();
        return ResponseEntity.ok(apiResponse);
    }
} 