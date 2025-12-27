package org.demo;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docshare.yangmvc.mvc.Controller;
import org.docshare.yangmvc.orm.Model;

public class UserController extends Controller {
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public List<Model> list() {
        logger.info("user list called");
        return Model.tool("user").all().toArrayList();
    }
}
