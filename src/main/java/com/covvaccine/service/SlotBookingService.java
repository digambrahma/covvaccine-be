package com.covvaccine.service;

import com.covvaccine.model.Center;
import com.covvaccine.model.Centers;
import com.covvaccine.model.Session;
import com.covvaccine.model.User;
import com.covvaccine.utils.BadRequestException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class SlotBookingService {

    private static final String STATUS = "status";

    private RestTemplate restTemplate;

    private JavaMailSender mailSender;

    private UserService userService;

    private static final Logger LOGGER = LoggerFactory.getLogger(SlotBookingService.class);

    @Autowired
    public SlotBookingService(RestTemplate restTemplate, JavaMailSender mailSender, UserService userService) {
        this.restTemplate = restTemplate;
        this.mailSender = mailSender;
        this.userService = userService;
    }

    public Map<String, Object> getVaccineSlots(@RequestParam String pincode, @RequestParam String date, @RequestParam String sendTo) throws ExecutionException, InterruptedException, BadRequestException {
        Map<String, Object> response = new HashMap<>();

        if (validateRequest(pincode, sendTo)) {
            LOGGER.error("Request does not contain required field");
            throw new BadRequestException();
        }

        User user = new User();
        user.setEmail(sendTo);
        user.setPincode(List.of(pincode));

        User getUser = null;

        if (!sendTo.isEmpty()) {
            getUser = userService.getUserDetailsByEmail(sendTo);
        }

        if (getUser != null && getUser.getPincode().stream().anyMatch(pin -> pin.equalsIgnoreCase(pincode)) && getUser.getEmail().equalsIgnoreCase(sendTo)) {
            response.put(STATUS, "Already registered!");
            LOGGER.info("User is already registered.");
            return response;
        } else if (getUser != null && getUser.getPincode().stream().noneMatch(pin -> pin.equalsIgnoreCase(pincode)) && getUser.getEmail().equalsIgnoreCase(sendTo)) {
            userService.savePincode(sendTo, pincode);
            LOGGER.info("Updated pincode for the user: {}", sendTo);
        } else if (!StringUtils.isEmpty(user.getEmail()) && !user.getPincode().isEmpty()) {
            userService.saveUser(user);
            LOGGER.info("User is saved in the database with following {} and {}", user.getEmail(), user.getPincode());
        }

        callFindSlotsAtInterval(pincode, date, sendTo);

        return callFindSlots(pincode, date, sendTo);
    }

    private void sentMail(String sendTo, List<Center> foundCenter) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(sendTo);
        message.setSubject("Corona Vaccine Slot Available!");
        StringBuilder builder = new StringBuilder();
        builder.append("Vaccine slot is available at: \n\n");
        AtomicInteger counter = new AtomicInteger(1);
        foundCenter.forEach(
                s -> builder.append(counter.getAndIncrement() + ".   " + s.getName() + "\n   " + s.getAddress() + " for age 18 and above " + "on " + s.getSessions().stream().map(Session::getDate).collect(Collectors.joining(", ")) + "\n\n"));
        builder.append("Please take your dose!\n\nThanks!\nDigambar \n");
        String bodyText = builder.toString();
        message.setText(bodyText);

        mailSender.send(message);
    }

    private void callFindSlotsAtInterval(String pincode, String date, String sendTo) {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                LOGGER.info("Calling find slots every 15 minutes!");
                String correctDate = checkDate(date);
                callFindSlots(pincode, correctDate, sendTo);
            }
        }, 0, 900000);
    }

    private Map<String, Object> callFindSlots(String pincode, String date, String sendTo) {
        Map<String, Object> response = new HashMap<>();
        response.put(STATUS, "No slots available!");
        try {
            LOGGER.info("Calling cowin public api with following: {} and {} ", pincode, sendTo);
            String url = "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByPin?pincode=" + pincode + "&date=" + date;

            HttpHeaders headers = new HttpHeaders();
            headers.add("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Centers> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, Centers.class);
            LOGGER.info("Cowin public api is called with status: {}", responseEntity.getStatusCode());

            if (!responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                response.put(STATUS, "Cowin public api call has failed due to "+ responseEntity.getBody().toString());
                return response;
            }

            var centers = responseEntity.getBody();
            List<Center> foundCenter = new ArrayList<>();

            if (centers != null) {
                foundCenter = centers.getCenters().stream().filter(cc -> cc.getSessions().stream().anyMatch(ss -> ss.getAvailableCapacity() != 0 && ss.getMinAgeLimit() == 18)).collect(Collectors.toList());
            }
            if (!foundCenter.isEmpty()) {
                sentMail(sendTo, foundCenter);
                response.put(STATUS, "Ok");
                LOGGER.info("Slots available! Email sent to the user.");
                return response;
            }

        } catch (Exception e) {
            LOGGER.error("Exception occured {}", e.getMessage());
        }
        LOGGER.info("No slots available!");
        return response;
    }

    private String checkDate(String date) {

        String dateToday1 = new SimpleDateFormat("dd-MM-yyyy").format(new Date());

        if (!dateToday1.equalsIgnoreCase(date)) {
            LOGGER.info("Changed date to Date Today!");
            return dateToday1;
        }
        return date;
    }

    private boolean validateRequest(String pincode, String email) {
        return StringUtils.isEmpty(pincode) || StringUtils.isEmpty(email);
    }
}
