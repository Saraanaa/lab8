package mn.edu.must.sqat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class CourseRegistrationServiceTest {

    @Mock
    StudentRepository repository;

    @Mock
    EmailSender emailSender;

    private CourseRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new CourseRegistrationService(repository, emailSender);
    }

    @Test
    void идэвхтэйОюутанБүртгүүлэхэдАмжилттайХадгалжИмэйлИлгээнэ() {
        Student student = new Student("S-001", "student@example.com", true, List.of("CS101"));
        when(repository.findStudent("S-001")).thenReturn(student);
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of());

        assertEquals("OK", service.register("S-001", "CS201"));

        verify(repository).saveRegistration("S-001", "CS201");
        verify(emailSender).send(eq("student@example.com"), anyString(), contains("CS201"));
    }

    @Test
    void байхгүйОюутанБүртгүүлэхэдХадгалалтБаИмэйлХийгдэхгүй() {
        when(repository.findStudent("missing")).thenReturn(null);

        assertEquals("ERROR_NO_STUDENT", service.register("missing", "CS201"));

        verify(repository, never()).saveRegistration(anyString(), anyString());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void идэвхгүйОюутанБүртгүүлэхэдИмэйлтэйХарьцахгүй() {
        Student student = new Student("S-002", "inactive@example.com", false, List.of());
        when(repository.findStudent("S-002")).thenReturn(student);

        assertEquals("ERROR_INACTIVE_STUDENT", service.register("S-002", "CS201"));

        verifyNoInteractions(emailSender);
    }

    @Test
    void дутууӨмнөхХолбооХичээлтэйБолБүртгэлХадгалахгүй() {
        Student student = new Student("S-003", "student@example.com", true, List.of());
        when(repository.findStudent("S-003")).thenReturn(student);
        when(repository.courseExists("CS301")).thenReturn(true);
        when(repository.prerequisites("CS301")).thenReturn(List.of("CS201"));

        assertEquals("ERROR_PREREQUISITES", service.register("S-003", "CS301"));

        verify(repository, never()).saveRegistration(anyString(), anyString());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void имэйлАлдааГарсанЧБүртгэлАмжилттайХэвээрҮлдэнэ() {
        Student student = new Student("S-004", "student@example.com", true, List.of());
        when(repository.findStudent("S-004")).thenReturn(student);
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of());
        doThrow(new RuntimeException("SMTP unavailable"))
                .when(emailSender).send(anyString(), anyString(), anyString());

        assertEquals("OK", service.register("S-004", "CS201"));

        verify(repository).saveRegistration("S-004", "CS201");
        verify(emailSender).send(anyString(), anyString(), contains("CS201"));
    }

    @Test
    void nullОролтӨгөхөдАлдааШидэжХамааралдХандахгүй() {
        assertThrows(IllegalArgumentException.class, () -> service.register(null, "CS201"));
        assertThrows(IllegalArgumentException.class, () -> service.register("S-001", null));

        verifyNoInteractions(repository, emailSender);
    }

    @Test
    void илгээгдсэнИмэйлийнБиедХичээлийнКодБагтсанБайна() {
        Student student = new Student("S-005", "student@example.com", true, List.of());
        when(repository.findStudent("S-005")).thenReturn(student);
        when(repository.courseExists("CS401")).thenReturn(true);
        when(repository.prerequisites("CS401")).thenReturn(List.of());

        service.register("S-005", "CS401");

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSender).send(eq("student@example.com"), anyString(), bodyCaptor.capture());
        assertEquals(true, bodyCaptor.getValue().contains("CS401"));
    }

    @ParameterizedTest(name = "{0} нөхцөлд {1} буцаана")
    @CsvSource({
            "байхгүй оюутан, ERROR_NO_STUDENT",
            "идэвхгүй оюутан, ERROR_INACTIVE_STUDENT",
            "байхгүй хичээл, ERROR_NO_COURSE"
    })
    void гурванАлдааныАнгидЗөвҮрДүнБуцаана(String scenario, String expectedResult) {
        Student activeStudent = new Student("S-006", "student@example.com", true, List.of());
        Student inactiveStudent = new Student("S-006", "student@example.com", false, List.of());

        // A8 мөр бүр өөр замаар явдаг тул ашиглагдахгүй stub-үүдийг lenient болгосон.
        lenient().when(repository.findStudent("missing")).thenReturn(null);
        lenient().when(repository.findStudent("inactive")).thenReturn(inactiveStudent);
        lenient().when(repository.findStudent("active")).thenReturn(activeStudent);
        lenient().when(repository.courseExists("missing-course")).thenReturn(false);
        lenient().when(repository.prerequisites("missing-course")).thenReturn(List.of());

        String result;
        switch (scenario) {
            case "байхгүй оюутан" -> result = service.register("missing", "CS201");
            case "идэвхгүй оюутан" -> result = service.register("inactive", "CS201");
            case "байхгүй хичээл" -> result = service.register("active", "missing-course");
            default -> throw new IllegalArgumentException("Unknown scenario: " + scenario);
        }

        assertEquals(expectedResult, result);
    }
}
