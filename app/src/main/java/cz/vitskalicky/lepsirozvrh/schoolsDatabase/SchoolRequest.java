package cz.vitskalicky.lepsirozvrh.schoolsDatabase;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class SchoolRequest extends Request<Void> {
    public static final String TAG = SchoolRequest.class.getSimpleName();

    private final Response.Listener<Void> listener;
    private final String url;
    private final SchoolDAO dao;

    public SchoolRequest(String url, SchoolDAO dao, Response.Listener<Void> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        this.listener = listener;
        this.dao = dao;
        this.url = url;
    }

    @Override
    protected void deliverResponse(Void response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<Void> parseNetworkResponse(NetworkResponse response) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(new ByteArrayInputStream(response.data));

            Element root = document.getDocumentElement();
            root.normalize();

            NodeList nodeList = root.getElementsByTagName("schoolInfo");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node nodeItem = nodeList.item(i);
                SchoolInfo schoolInfo = new SchoolInfo();

                NodeList childNodes = nodeItem.getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    Node nodeItem2 = childNodes.item(j);
                    switch (nodeItem2.getNodeName()) {
                        case "id":
                            schoolInfo.id = nodeItem2.getTextContent();
                            break;
                        case "name":
                            schoolInfo.name = nodeItem2.getTextContent();
                            break;
                        case "schoolUrl":
                            schoolInfo.url = nodeItem2.getTextContent();
                            break;
                    }
                }
                schoolInfo.setSearchText(schoolInfo.name, schoolInfo.url);
                dao.insertSchool(schoolInfo);
            }
            return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            Log.e(TAG, "Parsing city school list failed: url: " + url + "response:\n" + response + "\n\n----------\nstack trace");
            e.printStackTrace();
            return Response.error(new ParseError());
        }
    }
}

