package com.tokenmall.admin;

import com.tokenmall.common.web.ApiResponse;
import com.tokenmall.seckill.SeckillService;
import com.tokenmall.seckill.dto.SeckillActivityRequest;
import com.tokenmall.seckill.dto.SeckillActivityResponse;
import com.tokenmall.seckill.entity.SeckillActivity;
import com.tokenmall.seckill.entity.SeckillRecord;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/seckill/activities")
@RequiredArgsConstructor
public class AdminSeckillController {

    private final SeckillService seckillService;

    @GetMapping
    public ApiResponse<List<SeckillActivityResponse>> list() {
        return ApiResponse.ok(seckillService.listAll());
    }

    @PostMapping
    public ApiResponse<SeckillActivity> create(@Valid @RequestBody SeckillActivityRequest request) {
        return ApiResponse.ok(seckillService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<SeckillActivity> update(
            @PathVariable Long id,
            @Valid @RequestBody SeckillActivityRequest request
    ) {
        return ApiResponse.ok(seckillService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        seckillService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}/records")
    public ApiResponse<List<SeckillRecord>> records(@PathVariable Long id) {
        return ApiResponse.ok(seckillService.records(id));
    }
}
