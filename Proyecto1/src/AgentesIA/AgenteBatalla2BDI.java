package AgentesIA;


import java.util.Map;
import java.util.Random;

import jadex.bdiv3.annotation.Belief;
import jadex.bdiv3.annotation.Plan;
import jadex.bdiv3.annotation.Trigger;
import jadex.bdiv3.features.IBDIAgentFeature;

import jadex.bridge.IInternalAccess;
import jadex.bridge.component.IMessageFeature;
import jadex.bridge.fipa.SFipa;
import jadex.bridge.service.types.message.MessageType;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentCreated;
import jadex.micro.annotation.AgentFeature;
import jadex.micro.annotation.AgentMessageArrived;
import jadex.micro.annotation.Description;

@Agent
@Description("Juego de batalla con agentes - Agente 2")
public class AgenteBatalla2BDI {
	
	@AgentFeature
	protected IBDIAgentFeature bdiFeature;
	
	// Agente
	@Agent
	protected IInternalAccess agent;
	
	// Parametros iniciales	
	@Belief
	protected int dim = 10;			// tablero dim x dim
	
	@Belief
	protected int sizeShip = 3;     // tamanho de nave

	@Belief
	protected int centi = 0;
	
	// Mensajes
	@Belief
	protected Map<String, Object> msg;

	@Belief
	protected MessageType mts;
	
	// Mensaje que se envia
	@Belief
	protected String situacion;

	@ Belief
	protected int latiActual = 0;		// latitud ataque actual

	@ Belief
	protected int longActual = 0;		// latitud ataque actual
		
	// Se guarda informacion de la flota propia
	@Belief
	protected int[][] FlotaAgente2 = new int[this.dim][this.dim];;	

	// Se crea para hacer seguimiento de las flotas atacadas
	@Belief
	protected int[][] check2 = new int[this.dim][this.dim];
			
	// Se crea para guardar informacion de la flota enemiga
	@Belief
	protected int[][] FlotaEnemiga2 = new int[this.dim][this.dim];

	
	@AgentCreated
	public void init() {				
		
		// Se crean los tableros con ceros
		for (int i=0; i<this.dim; i++) {
			for (int j=0; j<this.dim; j++) {
				this.FlotaAgente2[i][j]=0;
			}
		}
								
		// Se crean aleatoriamente barcos en cada columna
		Random rand = new Random(10);
		for (int i=0; i<this.dim; i++) {			
			// El barco no puede ser mayor que el tamanho del tablero
			int ranGen = rand.nextInt(this.dim-this.sizeShip+1);
								
			for (int j=ranGen; j< ranGen+this.sizeShip; j++) {
				this.FlotaAgente2[j][i]=1;
			}
		}
		
		// Actualizacion de matriz de chequeo
		for (int i=0; i<this.dim; i++) {
			for (int j=0; j<this.dim; j++) {
				this.check2[i][j]=this.FlotaAgente2[i][j];
			}
		}
		
		// Actualizacion del tablero del jugador 1
		for (int i=0; i<this.dim; i++) {
			for (int j=0; j<this.dim; j++) {
				this.FlotaEnemiga2[i][j]=0;
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
		String msgcheck = (String) this.msg.get(SFipa.CONTENT);
		
		// Se verifica si el ataque anterior impacto
		String situa = (String) msgcheck.substring(36, msgcheck.length());
				
		// Se actualiza el tablero del jugador contrario
		// 0:No revisado, 1 revisado, 2: impactado
		if (situa.trim().equals("herido")  || situa.trim().equals("hundido")) {
			this.FlotaEnemiga2[this.latiActual][this.longActual] = 2;
		}
		if (situa.trim().equals("agua")) {
			this.FlotaEnemiga2[this.latiActual][this.longActual] = 1;				
		}
		
		// Se extrae la posicion atacada
		int lati2 = Integer.parseInt(msgcheck.substring(28, 29));
		int long2 = Integer.parseInt(msgcheck.substring(31, 32));
		
		// Se revisa si hay algun impacto		
		this.situacion = "agua";
		if (this.check2[lati2][long2] == 1) {
			this.check2[lati2][long2] = 0;
			this.FlotaAgente2[lati2][long2] = 2; // En el tablero original se coloca 2
			
			// Se revisa si el barco fue hundido
			if (this.hundido(check2, long2)) {
				this.situacion = "hundido";
			}else {
				this.situacion = "herido";
			}			
		}
					
		// Se revisa que aun se tiene barcos
		if(this.persisElem(this.check2)==false) {
			this.situacion = "termino";
		}
		
		Map<String, Object> reply2 = this.mts.createReply(msg);		
					
		// Se verifica si el juego no acabo		
		if (!this.situacion.equals("termino")) {
			
			
			// Se busca el siguiente elemento por filas
			// Se utiliza una estrategia que visita todos los nodos de manera secuencial
			int centinela = 0;
			saliloop:
			for (int i=0; i<this.dim; i++) {
				for (int j=0; j<this.dim; j++) {				
					// Nodos no visitados
					if (this.FlotaEnemiga2[i][j]==0) {
						this.latiActual = i;
						this.longActual =j;
						centinela = 1;
						break saliloop;
					}
				}
			}	

			if (centinela==1) {			
				
				System.out.println("Jugador2");				
				String content = "Ataco las coordenadas i,j: (" + String.valueOf(this.latiActual) + ", " + String.valueOf(this.longActual) + ") - " + this.situacion;			
				System.out.println(content);
				
				// Se muestra el tablero del jugador 2		
				System.out.println("Tablero del jugador 2");		
				// Se imprime el tablero 
				for (int i=0; i<this.dim; i++) {
					for (int j=0; j<this.dim; j++) {				
						System.out.print(this.FlotaAgente2[i][j] + " ");
					}
					System.out.println();
				}									
				
				reply2.put(SFipa.CONTENT, content);
				reply2.put(SFipa.PERFORMATIVE, SFipa.INFORM);
				reply2.put(SFipa.SENDER, agent.getComponentIdentifier());			
				agent.getComponentFeature(IMessageFeature.class).sendMessage(reply2, this.mts);			
			}							
		}
		else {
							
			if ( this.centi ==0) {
				
				System.out.println("\n \n");		
				System.out.println("*******************************");		
				System.out.println("El Jugador 1 gano el juego");		
				// Se muestra el tablero del perdedor
				
				System.out.println("Tablero del jugador 2: \n");
				// Se imprime el tablero del perdedor
				for (int i=0; i<this.dim; i++) {
					for (int j=0; j<this.dim; j++) {				
						System.out.print(this.FlotaAgente2[i][j] + " ");
					}
					System.out.println();
				}									
				
				// Solo se imprime una vez los resultados
				this.centi++;
			}				
		}
	}

	@AgentMessageArrived
	public void messageArrived(Map<String, Object> msg, final MessageType mt){
				
		this.msg = msg;						
		this.mts = mt;
		System.out.println(" ");
	}	
}
