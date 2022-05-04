package Batalla;

import java.util.HashMap;
import java.util.Map;

import jadex.bridge.fipa.SFipa;

public class Ejecutar {
	
	public static void main(String[] args) {
		
		System.out.println();
		AgenteBatalla x1 = new AgenteBatalla(4);
		AgenteBatalla x2 = new AgenteBatalla(4);
		x1.ejemplo();
		
		x1.executeBody();
		
		Map<String, Object> msg = new HashMap<String, Object>();
		int coor = 23;
		msg.put(SFipa.CONTENT,coor);
		x2.messageArrived(null, null);
		
	}

}
