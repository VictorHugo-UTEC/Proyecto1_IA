package AgentesIA;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Plan;
import jadex.bridge.BasicComponentIdentifier;
import jadex.bridge.IComponentIdentifier;
import jadex.bridge.IComponentStep;
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
import jadex.micro.annotation.AgentCreated;
import jadex.micro.annotation.AgentMessageArrived;
import jadex.micro.annotation.Description;
import jadex.micro.annotation.Arguments;
import jadex.micro.annotation.Argument;

import jadex.bdiv3.annotation.Trigger;

@Description("Juego de batalla con agentes - Agente 1")
@Arguments({
	@Argument(name="receiver", clazz=IComponentIdentifier.class, description="The component receiver of the ping target."),
	@Argument(name="missed_max", clazz=int.class, description="Maximum number of allowed missed replies", defaultvalue="3"),
	@Argument(name="timeout", clazz=long.class, description="Timeout for reply", defaultvalue="1000"),
	@Argument(name="content", clazz=String.class, description="Ping message content", defaultvalue="\"ping\"")
})

@Agent
public class AgenteBatalla1BDI {

	// agente
	@Agent
	protected IInternalAccess agent;
	
	// receptor
	protected IComponentIdentifier receiver;
	
	// Informacion enviada y recibida
	protected int dif;	
	protected Set<String> sent;

	// Mensajes
	@Belief
	protected Map<String, Object> msg;

	@Belief
	protected MessageType mts;
	
	// Mensaje que se envia
	@Belief
	protected String situacion;
	
	// Parametros iniciales	
	@Belief
	protected int dim = 10;			// tablero dim x dim
	
	@Belief
	protected int sizeShip = 3;     // tamanho de nave

	@Belief
	protected int centi = 0;
		
	@ Belief
	protected int latiActual = 0;		// latitud ataque actual

	@ Belief
	protected int longActual = 0;		// latitud ataque actual
		
	// Se guarda informacion de la flota propia
	@Belief
	protected int[][] FlotaAgente1 = new int[this.dim][this.dim];

	// Se crea para hacer seguimiento de las flotas atacadas
	@Belief
	protected int[][] check1 = new int[this.dim][this.dim];

	// Se crea para guardar informacion de la flota enemiga
	@Belief
	protected int[][] FlotaEnemiga1 = new int[this.dim][this.dim];
	
	
	@AgentCreated
	public void init() {				
		
		// Se crean los tableros con ceros
		for (int i=0; i<this.dim; i++) {
			for (int j=0; j<this.dim; j++) {
				this.FlotaAgente1[i][j]=0;
			}
		}
								
		// Se crean aleatoriamente barcos en cada columna
		Random rand = new Random(20);
		for (int i=0; i<this.dim; i++) {			
			// El barco no puede ser mayor que el tamanho del tablero
			int ranGen = rand.nextInt(this.dim-this.sizeShip+1);
								
			for (int j=ranGen; j< ranGen+this.sizeShip; j++) {
				this.FlotaAgente1[j][i]=1;				
			}
		}
		
		// Actualizacion de matriz de chequeo
		for (int i=0; i<this.dim; i++) {
			for (int j=0; j<this.dim; j++) {
				this.check1[i][j]=this.FlotaAgente1[i][j];
			}
		}
		
		// Actualizacion del tablero del jugador 2
		for (int i=0; i<this.dim; i++) {
			for (int j=0; j<this.dim; j++) {
				this.FlotaEnemiga1[i][j]=0;
			}
		}		
	}
	
	// Funcion para calcular que aun hay barcos 
	public Boolean persisElem(int[][] matrix) {
		int check = 0;
		for (int i=0; i<matrix.length; i++) {
			for (int j=0; j<matrix.length; j++) {
				check += matrix[i][j];
			}
		}
		return check>=1;
	}
	
	// Funcion para calcular si se el barco se hundio
	public Boolean hundido(int[][] matrix, int column) {
		int check = 0;
		for (int i=0; i<matrix.length; i++) {
			check+= matrix[i][column];
		}
		return check == 0;
	}
	
		
	@Plan(trigger=@Trigger(factchangeds="msg"))
	protected void removeEntry() {
		// Se extrae el mensaje del otro jugador
		String msgcheck =  (String) this.msg.get(SFipa.CONTENT);
		
		// Se verifica si el ataque anterior impacto
		String situa = (String) msgcheck.substring(36, msgcheck.length());
				

		// Seguimiento de flota enemiga 0:No revisado, 1 revisado, 2: impactado
		if (situa.trim().equals("herido") || situa.trim().equals("hundido")) {
			this.FlotaEnemiga1[this.latiActual][this.longActual] = 2;
		}
		else if(situa.trim().equals("agua")) {
			this.FlotaEnemiga1[this.latiActual][this.longActual] = 1;				
		}

		// Se extrae la posicion atacada
		int lati1 = Integer.parseInt(msgcheck.substring(28, 29));
		int long1 = Integer.parseInt(msgcheck.substring(31, 32));
		
		// Se revisa si hay algun impacto
		// tabla FlotaAgente1: 0->agua, 1->parte de barco activo, 2 ->parte de barco hundido
		this.situacion = "agua";
		if (this.check1[lati1][long1] == 1) {
			this.check1[lati1][long1] = 0;
			this.FlotaAgente1[lati1][long1] = 2; // En el tablero original se coloca 2
			
			// Se revisa si el barco fue hundido
			if (this.hundido(check1, long1)) {
				this.situacion = "hundido";
			}else {
				this.situacion = "herido";
			}			
		}
		
		// Se revisa que aun se tiene barcos
		if(this.persisElem(this.check1)==false) {
			this.situacion = "termino";
		}			
		
		// Se prepara el nuevo ataque
		Map<String, Object> reply1 = this.mts.createReply(msg);
		
		if (!this.situacion.equals("termino")) {
			int centinela = 0;
			
			saliloop:
			for (int i=0; i<this.dim; i++) {
				for (int j=0; j<this.dim; j++) {		
					// Nodos no visitados
					if (this.FlotaEnemiga1[i][j]==0) {
						this.latiActual = i;
						this.longActual =j;
						centinela = 1;
						break saliloop;
					}
				}
			}
			if (centinela==1) {
				System.out.println("Jugador1");				
				String content = "Ataco las coordenadas i,j: (" + String.valueOf(this.latiActual) + ", " + String.valueOf(this.longActual) + ") - " + this.situacion;
				System.out.println(content);
				
				// Se muestra el tablero del jugador 1		
				System.out.println("Tablero del jugador 1");		
				// Se imprime el tablero 
				for (int i=0; i<this.dim; i++) {
					for (int j=0; j<this.dim; j++) {				
						System.out.print(this.FlotaAgente1[i][j] + " ");
					}
					System.out.println();
				}									
								
				reply1.put(SFipa.CONTENT, content);
				reply1.put(SFipa.PERFORMATIVE, SFipa.INFORM);
				reply1.put(SFipa.SENDER, agent.getComponentIdentifier());
				//agent.getComponentFeature(IExecutionFeature.class).waitForDelay(500).get();
				agent.getComponentFeature(IMessageFeature.class).sendMessage(reply1, this.mts);			
			}
		}	
		else {
			if ( this.centi ==0) {
				System.out.println("El Jugador 2 gano el juego");		
				// Se muestra el tablero del perdedor
				
				System.out.println("Tablero del jugador 1: \n");
				// Se imprime el tablero del perdedor
				for (int i=0; i<this.dim; i++) {
					for (int j=0; j<this.dim; j++) {				
						System.out.print(this.FlotaAgente1[i][j] + " ");
					}
					System.out.println();
				}									
				
				// Solo se imprime una vez los resultados
				this.centi++;
			}
						
			String convid = (String)msg.get(SFipa.CONVERSATION_ID);
			sent.remove(convid);
		}									
	}

	@AgentBody
	public IFuture<Void> executeBody(){
								
		final Future<Void> ret = new Future<Void>();
		
		receiver = (IComponentIdentifier)agent.getComponentFeature(IArgumentsResultsFeature.class).getArguments().get("receiver");
		final int missed_max = ((Number)agent.getComponentFeature(IArgumentsResultsFeature.class).getArguments().get("missed_max")).intValue();
		final long timeout = ((Number)agent.getComponentFeature(IArgumentsResultsFeature.class).getArguments().get("timeout")).longValue();
		sent = new HashSet<String>();

		/***************************************************************************************/
		System.out.println("\n Se inicia el juego \n");		
		
		final Object content = "Ataco las coordenadas i,j: (" + String.valueOf(this.latiActual) + ", " + String.valueOf(this.longActual) + ") - " + this.situacion;
		
		final IComponentStep<Void> step = new IComponentStep<Void>(){

			public IFuture<Void> execute(IInternalAccess ia){
				if(dif>missed_max){
					agent.getLogger().warning("Ping target does not respond: "+receiver);
					ret.setResult(null);
				}
				else{
					String convid = SUtil.createUniqueId(agent.getComponentIdentifier().getName());
					Map<String, Object> msg = new HashMap<String, Object>();
					msg.put(SFipa.CONTENT, content);
					msg.put(SFipa.PERFORMATIVE, SFipa.QUERY_IF);
					msg.put(SFipa.CONVERSATION_ID, convid);
					msg.put(SFipa.RECEIVERS, new IComponentIdentifier[]{receiver});
					dif++;
					sent.add(convid);
					agent.getComponentFeature(IMessageFeature.class).sendMessage(msg, SFipa.FIPA_MESSAGE_TYPE);
					agent.getComponentFeature(IExecutionFeature.class).waitForDelay(timeout, this);
				}
				return IFuture.DONE;
			}
		};
				
		if(receiver==null){
			receiver = new BasicComponentIdentifier("myAgent2", agent.getComponentIdentifier().getParent());
		}		
		agent.getComponentFeature(IExecutionFeature.class).scheduleStep(step);		
		
		return ret;				
	}

	
	@AgentMessageArrived
	public void messageArrived(Map<String, Object> msg, MessageType mt)
	{		
		// Se lanza el plan cuando cambia el mensaje
		this.msg = msg;
		this.mts = mt;	
		System.out.println(" ");
	}	
}
