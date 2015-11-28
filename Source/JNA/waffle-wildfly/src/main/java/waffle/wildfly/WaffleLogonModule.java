package waffle.wildfly;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;

import waffle.jaas.WindowsLoginModule;

import java.security.Principal;
import java.util.Map;

public class WaffleLogonModule extends WindowsLoginModule {

    private Subject         subject;
    private Principal       principal;
    private Map             sharedState;
    private CallbackHandler callbackHandler;

    @Override
    public void initialize(Subject newSubject, CallbackHandler newCallbackHandler, Map<String, ?> newSharedState,
            Map<String, ?> options) {
        this.subject = newSubject;
        this.sharedState = newSharedState;
        this.callbackHandler = newCallbackHandler;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean login() throws LoginException {

        NameCallback nc = new NameCallback("name");
        PasswordCallback pc = new PasswordCallback("password", false);
        try {
            this.callbackHandler.handle(new Callback[] { nc, pc });
        } catch (Exception x) {
            throw new LoginException(x.getMessage());
        }

        String name = nc.getName();
        char[] passwordChar = pc.getPassword();
        String credential = passwordChar != null ? new String(passwordChar) : null;

        long loginTime = Long.parseLong(credential);
        if (loginTime < WaffleAuthenticationMechanism.UPTIME) {
            return false;
        }

        SimplePrincipal simplePrincipal = new SimplePrincipal(name, credential);

        this.sharedState.put("javax.security.auth.login.name", simplePrincipal.getName());
        this.sharedState.put("javax.security.auth.login.password", simplePrincipal.getCredential());

        this.principal = simplePrincipal;
        return true;
    }

    @Override
    public boolean commit() throws LoginException {
        if (this.principal == null) {
            return false;
        }
        this.subject.getPrincipals().add(this.principal);
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        return false;
    }

    @Override
    public boolean logout() throws LoginException {
        return false;
    }
}
