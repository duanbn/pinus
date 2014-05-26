package com.pinus.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * 解析xml的辅助工具.
 * 
 * @author duanbn
 * 
 */
public class XmlUtil {

	public static final Logger LOG = Logger.getLogger(XmlUtil.class);

	private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	private Document xmlDoc;

	/**
	 * 构造方法. 读取classpath根路径下的xml文件.
	 * 
	 * @param xmlFileName
	 *            classpath根路径下的xml文件名
	 */
	private XmlUtil(String xmlFileName) {
		InputStream is = null;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream(xmlFileName);
			xmlDoc = builder.parse(new InputSource(is));
		} catch (Exception e) {
			throw new RuntimeException("读取classpath根路径的xml失败, file name " + xmlFileName);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				LOG.error(e);
			}
		}
	}

	/**
	 * 构造方法. 读取指定的xml文件
	 * 
	 * @param xmlFile
	 *            指定的xml文件
	 */
	private XmlUtil(File xmlFile) {
		InputStream is = null;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			is = new FileInputStream(xmlFile);
			xmlDoc = builder.parse(new InputSource(is));
		} catch (Exception e) {
			throw new RuntimeException("读取classpath根路径的xml失败, file " + xmlFile.getAbsolutePath());
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				LOG.error(e);
			}
		}
	}

	public static XmlUtil getInstance(String xmlFileName) {
		return new XmlUtil(xmlFileName);
	}

	public static XmlUtil getInstance(File xmlFile) {
		return new XmlUtil(xmlFile);
	}

	public Node getRoot() {
		NodeList childs = xmlDoc.getChildNodes();
		if (childs == null || childs.getLength() == 0) {
			return null;
		}
		return xmlDoc.getChildNodes().item(0);
	}

	public Node getFirstChildByName(Node parent, String name) {
		NodeList childs = parent.getChildNodes();
		if (childs == null || childs.getLength() == 0) {
			return null;
		}

		Node node = null;
		for (int i = 0; i < childs.getLength(); i++) {
			node = childs.item(i);
			if (node.getNodeName().equals(name)) {
				return node;
			}
		}

		return null;
	}

	public List<Node> getChildByName(Node parent, String name) {
		List<Node> list = new ArrayList<Node>();
		NodeList childs = parent.getChildNodes();
		if (childs == null || childs.getLength() == 0) {
			return null;
		}

		Node node = null;
		for (int i = 0; i < childs.getLength(); i++) {
			node = childs.item(i);
			if (node.getNodeName().equals(name)) {
				list.add(node);
			}
		}

		return list;
	}

	public String getAttributeValue(Node node, String attribute) {
		NamedNodeMap namedNodeMap = node.getAttributes();
		if (namedNodeMap == null) {
			return null;
		}

		Node attrNode = namedNodeMap.getNamedItem(attribute);
		if (attrNode == null) {
			return null;
		}

		return attrNode.getNodeValue();
	}

}
