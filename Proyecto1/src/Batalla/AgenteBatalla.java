package Batalla;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bdiv3.runtime.ChangeEvent;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IInternalAccess;
import jadex.bridge.component.IArgumentsResultsFeature;
import jadex.bridge.component.IExecutionFeature;
import jadex.bridge.component.IMessageFeature;
import jadex.bridge.fipa.SFipa;
import jadex.bridge.service.types.message.MessageType;
import jadex.commons.SUtil;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentFeature;
import jadex.micro.annotation.AgentMessageArrived;
import jadex.micro.annotation.Description;
import jadex.rules.eca.ChangeInfo;
import jadex.bdiv3.annotation.*;

@Agent
@Description("Proyecto N1")
public class AgenteBatalla{
	
	int dim=4;//tablero dim x dim
	protected IInternalAccess agent;
	
	@Belief
	char[][] FlotaPropia = new char[this.dim][this.dim];
	
	@Belief
	char[][] FlotaEnemiga = new char[this.dim][this.dim];
	
	@Belief(dynamic=true)
	protected boolean FinJuego = (FlotaPropia.length==0);
	
	AgenteBatalla(int dimensiones){
		this.dim = dimensiones;	
	}
	
	/** The receiver. */
	protected IComponentIdentifier receiver;
	/** The difference between sent messages and received replies. */
	protected int dif;
	/** Hashset with conversation ids of sent messages. */
	protected Set<String> sent;
	
	
	@AgentFeature
	protected IBDIAgentFeature bdiFeature; // Eventos del agente
	
	
	@Goal(recur=true) // se pide que sea recurrente.
    public class HundirFlota{
    }
	
	@Plan(trigger=@Trigger(goals=HundirFlota.class))
	public void Disparos(ChangeEvent event)
	{
	  //Aqui se debe colocar el algoritmo para realizar los disparos de manera de undir a la flota contraria.
	}
	

	@AgentBody
	public void executeBody()
	//public IFuture<Void> executeBody()
	{
		Map<String, Object> msg = new HashMap<String, Object>();
		int coor = 23;
		msg.put(SFipa.CONTENT,coor);
		agent.getComponentFeature(IMessageFeature.class).sendMessage(msg, SFipa.FIPA_MESSAGE_TYPE);
		//return content;
	}
	
	public void ejemplo () {
		for (int i = 0; i<this.dim;i++) {
			for (int j=0;j<this.dim;j++) {
				this.FlotaPropia[i][j] = '-';
			}
		}
		System.out.println(Arrays.deepToString(this.FlotaPropia));
	}
	
	@AgentMessageArrived
	public void messageArrived(Map<String, Object> msg, MessageType mt)
	{
		//La replica es "hundido", "herido" y "agua".
		//
		
		if(mt.equals(SFipa.FIPA_MESSAGE_TYPE))
		{
			String mrecibido = (String)msg.get(SFipa.CONTENT);
			System.out.println("Mensaje recibido: " + mrecibido);
			
		}
	}

}
