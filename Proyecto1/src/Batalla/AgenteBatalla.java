package Batalla;

import java.util.Arrays;


import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Goal;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.features.IBDIAgentFeature;
import jadex.bdiv3.runtime.ChangeEvent;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.AgentFeature;
import jadex.micro.annotation.Description;
import jadex.rules.eca.ChangeInfo;
import jadex.bdiv3.annotation.*;

@Agent
@Description("Proyecto N1")
public class AgenteBatalla {
	
	int l;
	
	@AgentFeature
	protected IBDIAgentFeature bdiFeature; // Eventos del agente
	
	@Belief
	char[][] FlotaPropia = new char[l][l];
	
	@Belief
	char[][] FlotaEnemiga = new char[l][l];
	
	@Belief(dynamic=true)
	protected boolean FinJuego = (FlotaPropia.length==0);
	
	@Goal // Es necesario hacerlo recurrente.
    public class HundirFlota{
		
		//Debe mandar un mensaje al agente contrario (Jugador contrario) con las posiciones X,Y.
		//La réplica es "hundido", "herido" y "agua".

        public Object get() {
            // TODO Auto-generated method stub
            return null;
        }

    }
	
	@Plan(trigger=@Trigger(factchangeds="FinJuego"))
	public void Ganador(ChangeEvent event)
	{
	  ChangeInfo<Boolean> change = (ChangeInfo<Boolean>)event.getValue();
	  // Print warning when value changes from false to true.
	  if(Boolean.FALSE.equals(change.getOldValue()) && Boolean.TRUE.equals(change.getValue()))
	  {
	    System.out.println("Warning, a colloquial word pair has been added.");
	  }
	}
	
	@AgentBody
	public IFuture<Void> executeBody()
	{
		final Future<Void> ret = new Future<Void>();
		
		return ret;
	}
	
	public void ejemplo () {
		for (int i = 0; i<10;i++) {
			for (int j=0;j<10;j++) {
				this.FlotaPropia[i][j] = '-';
			}
		}
		
		System.out.println(Arrays.deepToString(this.FlotaPropia));
	}

}
