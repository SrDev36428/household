package cz.cvut.fit.household.datamodel.entity.events;

import cz.cvut.fit.household.datamodel.entity.user.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;

@Getter
@Setter
public class OnRegistrationCompleteEvent extends ApplicationEvent {

    private Locale locale;
    private transient User user;

    public OnRegistrationCompleteEvent(Locale locale, User user) {
        super(user);
        this.locale = locale;
        this.user = user;
    }
}
