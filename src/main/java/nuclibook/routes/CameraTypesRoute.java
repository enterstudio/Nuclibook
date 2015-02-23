package nuclibook.routes;

import nuclibook.entity_utils.CameraTypeUtils;
import nuclibook.models.CameraType;
import nuclibook.server.HtmlRenderer;
import spark.Request;
import spark.Response;

import java.util.List;

public class CameraTypesRoute extends DefaultRoute {

	@Override
	public Object handle(Request request, Response response) throws Exception {
		prepareToHandle();

		// start renderer
		HtmlRenderer renderer = getRenderer();
		renderer.setTemplateFile("camera-types.html");

		// get camera types and add to renderer
		List<CameraType> allCameraTypes = CameraTypeUtils.getAllCameraTypes(true);
		renderer.setCollection("camera-types", allCameraTypes);

		return renderer.render();
	}
}
