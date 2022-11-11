package com.orange.rouber.controller;

import com.orange.rouber.client.VehicleDto;
import com.orange.rouber.converter.Converters;
import com.orange.rouber.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.orange.rouber.converter.Converters.toVehicleDtos;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }


    @Operation(summary = "Registers a vehicle. Vehicle registration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized"),
            @ApiResponse(responseCode = "404", description = "Data not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void registerVehicle(@RequestBody VehicleDto vehicleDto) {
        vehicleService.registerVehicle(Converters.toVehicle(vehicleDto), vehicleDto.getOwnerId());
    }

    @GetMapping("/history")
    public List<VehicleDto> getVehicleHistory() {
        return toVehicleDtos(vehicleService.getVehicleHistory());
    }
}
