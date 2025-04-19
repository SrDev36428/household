package cz.cvut.fit.household.controller;

import cz.cvut.fit.household.config.security.SecurityConfig;
import cz.cvut.fit.household.datamodel.entity.user.User;
import cz.cvut.fit.household.datamodel.entity.user.UserRegistrationDTO;
import cz.cvut.fit.household.datamodel.entity.user.VerificationToken;
import cz.cvut.fit.household.service.interfaces.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringRunner.class)
@WebMvcTest(RegistrationController.class)
@Import(SecurityConfig.class)
@ContextConfiguration
public class RegistrationControllerTest {


    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private ApplicationEventPublisher eventPublisher;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HttpServletRequest httpServletRequest;

    User user1 = new User("user1","1","User","1","user1@gmail.com", new ArrayList<>());
    User user2 = new User("user2","2","User","2","user2@gmail.com",new ArrayList<>());
    User userNotExisted = new User("","null",null,null,null,null);
    User userNotRegistered = new User("user_not_registered","3","User","not_registered","user_not_registered@gmail.com", new ArrayList<>());
    String token="token";
    String tokenExpired="tokenExpired";
    VerificationToken verificationToken= new VerificationToken(token,user1);
    VerificationToken verificationTokenExpired= new VerificationToken(token,user1);
    UserRegistrationDTO userRegistrationDTO= new UserRegistrationDTO(userNotRegistered,userNotRegistered.getPassword());


    @Before
    public void setup() {
        verificationToken.setExpirationDate(LocalDate.of(2223,1,1));
        verificationTokenExpired.setExpirationDate(LocalDate.of(1999,9,9));
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        when(userService.findUserByUsername(user1.getUsername())).thenReturn(java.util.Optional.ofNullable(user1));
        when(userService.getVerificationToken(token)).thenReturn(verificationToken);
        when(userService.getVerificationToken(tokenExpired)).thenReturn(verificationTokenExpired);
    }

    @Test
    public void defaultPageTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());
    }

    @Test
    public void renderLoginPageTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/login"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    public void renderSignUpPageTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/signup"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("signUp"));
    }

//    @Test
//    public void signUpSuccessTest() throws Exception {
//        mockMvc.perform(MockMvcRequestBuilders.post("/signup")
//                       .flashAttr("userRegistrationDTO",userRegistrationDTO))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(view().name("sign-up-successful"));
//    }

    @Test
    public void confirmRegistrationTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/registrationConfirm")
                        .param("token",token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("registrationConfirm"));
    }

    @Test//(expected = Exception.class)
    public void confirmRegistrationTokenNotExistTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/registrationConfirm")
                        .param("token",""))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
        //.andExpect(view().name("registrationConfirm"));
    }

    @Test//(expected = Exception.class)
    public void confirmRegistrationTokenExpiredTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/registrationConfirm")
                        .param("token",tokenExpired))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
    @Test
    public void renderWelcomePageTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/welcome")
                        .with(user(user1.getUsername()).password(user1.getPassword())))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("welcome"));
    }




}
