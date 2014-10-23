package com.podhoarder.util;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class FileUtils
{
	/**
	 * Makes sure that a file name is within the OS rules for naming files. (length, special chars etc.)
	 * @param fileName	The file name you want sanitized.
	 * @return A sanitized version of fileName.
	 */
	public static String sanitizeFileName(String fileName)
	{
		String retString = fileName;
		retString = retString.replaceAll("[^a-zA-Z0-9_\\-\\.]", "_");
		if (retString.length() > 30) retString = retString.substring(0, 25);
		return retString;
	}

    public static String cacheXML(Document xml)
    {
        Transformer transformer = null;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            File outputFile =  File.createTempFile("tmp", ".xml");
            Result output = null;
            output = new StreamResult(outputFile);
            Source input = new DOMSource(xml);
            transformer.transform(input, output);
            return outputFile.getPath();
        }
        catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Document loadCachedXml(String fileName)
    {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(false);
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            File xmlFile = new File(fileName);
            Document res =  db.parse(xmlFile);
            xmlFile.delete();
            return res;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
