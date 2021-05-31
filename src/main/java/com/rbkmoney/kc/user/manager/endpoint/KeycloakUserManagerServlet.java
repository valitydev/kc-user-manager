package com.rbkmoney.kc.user.manager.endpoint;

import com.rbkmoney.kc_user_manager.KeycloakUserManagerSrv;
import com.rbkmoney.woody.api.event.CompositeServiceEventListener;
import com.rbkmoney.woody.thrift.impl.http.THServiceBuilder;
import com.rbkmoney.woody.thrift.impl.http.event.HttpServiceEventLogListener;
import com.rbkmoney.woody.thrift.impl.http.event.ServiceEventLogListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.GenericServlet;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@WebServlet("/v1/keycloak/users")
public class KeycloakUserManagerServlet extends GenericServlet {

    private final KeycloakUserManagerSrv.Iface requestHandler;

    private Servlet thriftServlet;

    @Override
    public void init(ServletConfig config) throws ServletException {
        log.info("kc-user-manager servlet init.");
        super.init(config);
        thriftServlet = new THServiceBuilder()
                .withEventListener(
                        new CompositeServiceEventListener<>(
                                new ServiceEventLogListener(),
                                new HttpServiceEventLogListener()
                        )
                )
                .build(KeycloakUserManagerSrv.Iface.class, requestHandler);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        thriftServlet.service(req, res);
    }
}
