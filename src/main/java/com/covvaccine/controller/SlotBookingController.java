package com.covvaccine.controller;

import com.covvaccine.service.SlotBookingService;
import com.covvaccine.utils.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
public class SlotBookingController {

    private SlotBookingService slotBookingService;

    @Autowired
    public SlotBookingController(SlotBookingService slotBookingService) {
        this.slotBookingService = slotBookingService;
    }

    @GetMapping(value = "/findslots")
    public Map<String, Object> getVaccineSlots(@RequestParam String pincode, @RequestParam String date, @RequestParam String sendTo) throws ExecutionException, InterruptedException, BadRequestException {
        return slotBookingService.getVaccineSlots(pincode, date, sendTo);
    }
}
