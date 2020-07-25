package com.studithm.modules.event;

import com.studithm.modules.account.Account;
import com.studithm.modules.account.CurrentAccount;
import com.studithm.modules.event.form.EventForm;
import com.studithm.modules.event.validator.EventValidator;
import com.studithm.modules.study.Study;
import com.studithm.modules.study.StudyService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/study/{path}")
@RequiredArgsConstructor
public class EventController {

    private final StudyService studyService;
    private final EventService eventService;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;
    private final EventRepository eventRepository;
    private final EnrollmentRepository enrollmentRepository;

    @InitBinder("eventForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(eventValidator);
    }

    @GetMapping("/new-event")
    public String newEventForm(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        model.addAttribute(study);
        model.addAttribute(account);
        model.addAttribute(new EventForm());

        return "event/form";
    }

    @PostMapping("/new-event")
    public String newEventSubmit(@CurrentAccount Account account, @PathVariable String path,
                                 @Valid EventForm eventForm, Errors errors, Model model) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            return "event/form";
        }

        Event event = eventService.createEvent(modelMapper.map(eventForm, Event.class), study, account);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }

    @GetMapping("/events/{eventId}")
    public String getEvent(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long eventId,
                           Model model) {
        model.addAttribute(account);
        model.addAttribute(eventRepository.findById(eventId).orElseThrow());
        model.addAttribute(studyService.getStudy(path));

        return "event/view";
    }

    @GetMapping("/events")
    public String viewStudyEvents(@CurrentAccount Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudy(path);
        model.addAttribute(account);
        model.addAttribute(study);

        List<Event> events = eventRepository.findByStudyOrderByStartDateTime(study);
        List<Event> newEvents = new ArrayList<>();
        List<Event> oldEvents = new ArrayList<>();
        events.forEach(e -> {
            if (e.getEndDateTime().isBefore(LocalDateTime.now()))
                oldEvents.add(e);
            else
                newEvents.add(e);
        });

        model.addAttribute("newEvents", newEvents);
        model.addAttribute("oldEvents", oldEvents);

        return "study/events";
    }

    @GetMapping("/events/{eventId}/edit")
    public String updateEventForm(@CurrentAccount Account account,
                                  @PathVariable String path, @PathVariable Long eventId, Model model) {
        Study study = studyService.getStudyToUpdate(account, path);
        Event event = eventRepository.findById(eventId).orElseThrow();
        model.addAttribute(study);
        model.addAttribute(account);
        model.addAttribute(event);
        model.addAttribute(modelMapper.map(event, EventForm.class));
        return "event/update-form";
    }

    @PostMapping("/events/{eventId}/edit")
    public String updateEventSubmit(@CurrentAccount Account account, @PathVariable String path,
                                    @PathVariable Long eventId, @Valid EventForm eventForm, Errors errors,
                                    Model model) {
        Study study = studyService.getStudyToUpdate(account, path);
        Event event = eventRepository.findById(eventId).orElseThrow();
        eventForm.setEventType(event.getEventType());
        eventValidator.validateUpdateForm(eventForm, event, errors);

        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute(event);
            return "event/update-form";
        }

        eventService.updateEvent(event, eventForm);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }

    @DeleteMapping("/events/{eventId}")
    public String cancelEvent(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long eventId) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        eventService.deleteEvent(eventRepository.findById(eventId).orElseThrow());
        return "redirect:/study/" + study.getEncodedPath() + "/events";
    }


    @PostMapping("/events/{eventId}/enroll")
    public String newEnrollment(@CurrentAccount Account account, @PathVariable String path, @PathVariable Long eventId,
                            Model model) {
        Study study = studyService.getStudyToEnroll(path);
        eventService.newEnrollment(eventRepository.findById(eventId).orElseThrow(), account);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + eventId;
    }

    @PostMapping("/events/{eventId}/disenroll")
    public String cancelEnrollment(@CurrentAccount Account account,
                                   @PathVariable String path, @PathVariable Long eventId) {
        Study study = studyService.getStudyToEnroll(path);
        eventService.cancelEnrollment(eventRepository.findById(eventId).orElseThrow(), account);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + eventId;
    }

    @PostMapping("/events/{eventId}/enrollments/{enrollmentId}/accept")
    public String acceptEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                   @PathVariable Long eventId, @PathVariable Long enrollmentId) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        Event event = eventRepository.findById(eventId).orElseThrow();
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        eventService.acceptEnrollment(event, enrollment);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + eventId;
    }

    @PostMapping("/events/{eventId}/enrollments/{enrollmentId}/reject")
    public String rejectEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                   @PathVariable Long eventId, @PathVariable Long enrollmentId) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        Event event = eventRepository.findById(eventId).orElseThrow();
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        eventService.rejectEnrollment(event, enrollment);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + eventId;
    }

    @PostMapping("/events/{eventId}/enrollments/{enrollmentId}/checkin")
    public String checkInEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                    @PathVariable Long eventId, @PathVariable Long enrollmentId) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        Event event = eventRepository.findById(eventId).orElseThrow();
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        eventService.checkInEnrollment(enrollment);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + eventId;
    }

    @PostMapping("/events/{eventId}/enrollments/{enrollmentId}/cancel-checkin")
    public String cancelCheckInEnrollment(@CurrentAccount Account account, @PathVariable String path,
                                          @PathVariable Long eventId, @PathVariable Long enrollmentId) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        Event event = eventRepository.findById(eventId).orElseThrow();
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow();
        eventService.cancelCheckInEnrollment(enrollment);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }
}
