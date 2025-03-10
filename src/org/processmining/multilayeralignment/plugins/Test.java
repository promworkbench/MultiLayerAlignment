package org.processmining.multilayeralignment.plugins;

import lpsolve.LpSolve;
import nl.tue.alignment.Progress;
import nl.tue.alignment.Utils;
import nl.tue.alignment.algorithms.ReplayAlgorithm;
import nl.tue.alignment.algorithms.ReplayAlgorithm.Debug;
import nl.tue.alignment.algorithms.implementations.AStarLargeLP;
import nl.tue.alignment.algorithms.syncproduct.SyncProduct;
import nl.tue.alignment.algorithms.syncproduct.SyncProductImpl;
import nl.tue.astar.util.ilp.LPMatrixException;


public class Test {

	
	public static byte LM = SyncProduct.LOG_MOVE;
	public static byte MM = SyncProduct.MODEL_MOVE;
	public static byte SM = SyncProduct.SYNC_MOVE;
	public static byte TM = SyncProduct.TAU_MOVE;
	//public static byte PM = SyncProduct.PSYNC_MOVE;
	//public static byte DM = SyncProduct.DLOG_MOVE;
	
	public static int[] NE = SyncProduct.NOEVENT;
	public static int NR = SyncProduct.NORANK;

	public static class SyncProductExampleBook extends SyncProductImpl {
	public SyncProductExampleBook() {
		super("Book Example", 10,
				new String[] { "As,-", "Aa,-", "Fa,-", "Sso,-", "Ro,-", "Co,-", "t,-", "Da1,-", "Do,-", "Da2,-",
						"Ao,-", "Aaa,-", "As,As", "Aa,Aa", "Sso,Sso", "Ro,Ro", "Ao,Ao", "Aaa,Aaa1", "Aaa,Aaa2",
						"-,As", "-,Aa", "-,Sso", "-,Ro", "-,Ao", "-,Aaa1", "-,Aaa2" }, //
				new String[] { "p0", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9", "p10", "p11", "p12",
						"p13", "p14", "p15", "p16", "p17", "p18" }, //
				new int[][] { NE, NE, NE, NE, NE, NE, NE, NE, NE, NE, NE, NE, { 0 }, { 1 }, { 2 }, { 3 }, { 4 },
						{ 5 }, { 6 }, { 0 }, { 1 }, { 2 }, { 3 }, { 4 }, { 5 }, { 6 } }, //
				new int[] { NR, NR, NR, NR, NR, NR, NR, NR, NR, NR, NR, NR, 0, 1, 2, 3, 4, 5, 6, 0, 1, 2, 3, 4, 5,
						6 }, //
				new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, //
				new byte[] { MM, MM, MM, MM, MM, MM, TM, MM, MM, MM, MM, MM, SM, SM, SM, SM, SM, SM, SM, LM, LM, LM,
						LM, LM, LM, LM }, //
				//					new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, /**/ 19, 20, 21, 22, 23, 24, 24, /**/ 12, 13, 14,
				//							15, 16, 17, 17 }, //
				new int[] { 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1 }//
		);
		setInput(0, 0);
		setInput(1, 1);
		setInput(2, 2);
		setInput(3, 3);
		setInput(4, 4);
		setInput(5, 6);
		setInput(6, 5, 6);
		setInput(7, 1);
		setInput(8, 7);
		setInput(9, 8);
		setInput(10, 7);
		setInput(11, 9);
		//SyncMoves
		setInput(12, 0, 11);
		setInput(13, 1, 12);
		setInput(14, 3, 13);
		setInput(15, 4, 14);
		setInput(16, 7, 15);
		setInput(17, 9, 16);
		setInput(18, 9, 17);
		//LogMoves
		setInput(19, 11);
		setInput(20, 12);
		setInput(21, 13);
		setInput(22, 14);
		setInput(23, 15);
		setInput(24, 16);
		setInput(25, 17);

		setOutput(0, 1);
		setOutput(1, 2, 3);
		setOutput(2, 5);
		setOutput(3, 4);
		setOutput(4, 6);
		setOutput(5, 3);
		setOutput(6, 7);
		setOutput(7, 10);
		setOutput(8, 8);
		setOutput(9, 10);
		setOutput(10, 9);
		setOutput(11, 10);
		setOutput(12, 1, 12);
		setOutput(13, 2, 3, 13);
		setOutput(14, 4, 14);
		setOutput(15, 6, 15);
		setOutput(16, 9, 16);
		setOutput(17, 10, 17);
		setOutput(18, 10, 18);
		setOutput(19, 12);
		setOutput(20, 13);
		setOutput(21, 14);
		setOutput(22, 15);
		setOutput(23, 16);
		setOutput(24, 17);
		setOutput(25, 18);

		setInitialMarking(0, 11);
		setFinalMarking(10, 18);
	}
	}
/*	
 *
	public static class SyncProductMultiPerspective extends SyncProductImpl {
		public SyncProductMultiPerspective() {
			super("MultiPerspective_CUD", 10,
					new String[] { "AD1_m", "A_p", "AD1_l", "AD1_t", "-,s1"}, //
					new String[] { "p0", "p1", "p2", "p3", "p4", "p5" }, //
					new int[][] { NE,{ 0 }, { 1 }, { 0}, { 2}}, //
					new int[] { NR, 0, 1, 0, 1 }, //
					new int[] { 1, 1, 1, 1, 1 }, //
					new byte[] { MM, PM, LM, SM, DM }, //
					new int[] { 2, 1, 2, 0, 2}//
			);
			setInput(0, 0);	
			setInput(1, 0, 2);
			setInput(2, 2);	
			setInput(3, 0, 2, 4);
			setInput(4, 4);
			
			
			setOutput(0, 1);
			setOutput(1, 1, 3);
			setOutput(2, 3);
			setOutput(3, 1, 3, 5);
			setOutput(4, 5);
			
			setInitialMarking(0, 2, 4);
			setFinalMarking(1, 3, 5);
			
		}
	}
	public static class SyncProductMultiPerspective1 extends SyncProductImpl {
		public SyncProductMultiPerspective1() {
			super("MultiPerspective_CUD", 10,
					new String[] {"AD1_m",
							      "AsD1_p", "AcD1_p",
							      "AsD1_l","AcD1_l",
							      "AsD1S1_t","AcD2S1_t",
							      "-,S1"}, //
					new String[] { "p0", "p1",
							       "p2",
							       "p3", "p4", "p5",
							       "p6",
							       "p7", "p8"}, //
					new int[][] { NE,
				                 { 0 }, { 1 }, 
				                 { 0}, { 1},
				                 { 0}, { 1},
				                 { 2}}, //
					new int[] { NR,
			                    0,1,
			                    0,1,
			                    0,1,
			                    2 }, //
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1 }, //
					new byte[] { MM, PM, PM,LM, LM, SM, SM, DM }, //
					new int[] { 2,1,1,2,2,0,0,2}//
			);
			setInput(0, 0);
			setOutput(0, 1);
			
			setInput(1, 0, 3);
			setOutput(1, 2, 4);

			setInput(2, 2, 4);
			setOutput(2, 1, 5);
			
			setInput(3,3 );
			setOutput(3,4 );
			
			setInput(4, 4);
			setOutput(4, 5);
			
			setInput(5, 0,3,7);
			setOutput(5, 4, 6);
			
			setInput(6, 4,6 );
			setOutput(6,1,5,8);
			
			setInitialMarking(0, 3, 7);
			setFinalMarking(1, 5, 8);
			
		}
	}
	
	public static class SyncProductMultiPerspective2 extends SyncProductImpl {
		public SyncProductMultiPerspective2() {
			super("MultiPerspective_CUDm", 10,
					new String[] {"AD1_m",
							      "AsD1_p", "AcD1_p",
							      "AsD1_l","AcD1_l"}, //
					new String[] { "p0", "p1",
							       "p2",
							       "p3", "p4", "p5",
							       "p6",
							       "p7", "p8"}, //
					new int[][] { NE,
				                 { 0 }, { 1 }, 
				                 { 0}, { 1}}, //
					new int[] { NR,
			                    0,1,
			                    0,1 }, //
					new int[] { 1, 1, 1, 1, 1 }, //
					new byte[] { MM, PM, PM,LM, LM }, //
					new int[] { 2,1,1,2,2}//
			);
			setInput(0, 0);
			setOutput(0, 1);
			
			setInput(1, 0, 3);
			setOutput(1, 2, 4);

			setInput(2, 2, 4);
			setOutput(2, 1, 5);
			
			setInput(3,3 );
			setOutput(3,4 );
			
			setInput(4, 4);
			setOutput(4, 5);
			
			
			setInitialMarking(0, 3);
			setFinalMarking(1, 5);
			
		}
	}
	
	public static class SyncProductMultiPerspective3 extends SyncProductImpl {
		public SyncProductMultiPerspective3() {
			super("MultiPerspective_CUDi", 10,
					new String[] {"T", "AD1_m","AD2_m","T",
							      "AsD1_p", "AcD1_p","AsD2_p", "AcD2_p",
							      "T", "AsD1_m","AsD2_m","T", "T", "AcD1_m","AcD2_m","T",
							      "AsD1S1_t", "AcD1S1_t",
							      "-,S1"}, //
					new String[] { "p0", "p1","p2","p3", "p4", "p5",
							       "p6", "p7",
							       "p8", "p9","p10","p11", "p12", "p13",
							       "p14", "p15","p16","p17", "p118",
							       "p19",
							       "p20", "p21"}, //
					new int[][] { NE,NE,NE,NE,
				                 { 0 }, { 2 },{ 1 }, { 3 }, 
				                 { 0 }, { 1 },{ 2 }, { 3 },
				                 NE,{0},{1},NE,NE,{2},{3},NE,
				                 {0},{2},
				                 {4}}, //
					new int[] { NR,NR,NR,NR,
			                 0,2,1,3, 
			                 0,1,2,3,
			                 NR,0,1,NR,NR,2,3,NR,
			                 0,2,
			                 4}, //
					new int[] { 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1 }, //
					new byte[] { TM, MM, MM,TM,
							     PM,PM,PM,PM,
							     TM,LM,LM,TM,TM,LM,LM,TM,
							     SM,SM,
							     DM}, //
					new int[] { 0, 2, 2,0,
						     1,1,1,1,
						     0,2,2,0,0,2,2,0,
						     0,0,
						     2}//
			);
			setInput(0, 0);
			setOutput(0,1,3);
			
			setInput(1,1);
			setOutput(1,2);

			setInput(2,3);
			setOutput(2, 4);
			
			setInput(3, 2,4 );
			setOutput(3,5);
			
			setInput(4,1,9);
			setOutput(4,6,10);
			
			setInput(5,6,14);
			setOutput(5,2,15);
			
			setInput(6,3,11);
			setOutput(6,7,12);
			
			setInput(7, 7,16);
			setOutput(7,4,17);
			
			setInput(8, 8);
			setOutput(8,9,11);
			
			setInput(9, 9);
			setOutput(9,10);
			
			setInput(10, 11);
			setOutput(10,12);
			
			setInput(11,10,12 );
			setOutput(11,13);
			
			setInput(12, 13 );
			setOutput(12,14,16);
			
			setInput(13, 14);
			setOutput(13,15);
			
			setInput(14, 16);
			setOutput(14,17);
			
			setInput(15, 15,17);
			setOutput(15,18);
			
			setInput(16,1,9,20);
			setOutput(16,10,19);
			
			setInput(17,14,19 );
			setOutput(17, 2,15,21);
			
			setInput(18,20);
			setOutput(18,21);
			
			setInitialMarking(0, 8, 20);
			setFinalMarking(5, 18, 21);
			
		}
	}
	
	public static class SyncProductMultiPerspective4 extends SyncProductImpl {
		public SyncProductMultiPerspective4() {
			super("MultiPerspective_Parallel", 10,
					new String[] {"T", "AD1_m","BD2_m","T",
							      "AsD1_p", "AcD1_p","BsD2_p", "BcD2_p",
							      "T", "AsD1_m","BsD2_m","T", "T", "AcD1_m","BcD2_m","T",
							      "AsD1S1_t", "AcD1S1_t","AsD1S2_t", "AcD1S2_t","BsD1S2_t", "BcD1S2_t",
							      "-,S1","-,S2"}, //
					new String[] { "p0", "p1","p2","p3", "p4", "p5",
							       "p6", "p7",
							       "p8", "p9","p10","p11", "p12", "p13",
							       "p14", "p15","p16","p17", "p118",
							       "p19", "p20", "p21",
							       "p22", "p23", "P24","P25"}, //
					new int[][] { NE,NE,NE,NE,
				                 { 0 }, { 2 },{ 1 }, { 3 }, 
				                 { 0 }, { 1 },{ 2 }, { 3 },
				                 NE,{0},{1},NE,NE,{2},{3},NE,
				                 {0},{2},{1},{3},
				                 {4},{5}}, //
					new int[] { NR,NR,NR,NR,
			                 0,2,1,3, 
			                 0,1,2,3,
			                 NR,0,1,NR,NR,2,3,NR,
			                 0,2,1,3,
			                 4,5}, //
					new int[] { 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1 }, //
					new byte[] { TM, MM, MM,TM,
							     PM,PM,PM,PM,
							     TM,LM,LM,TM,TM,LM,LM,TM,
							     SM,SM,SM,SM,SM,SM,
							     DM,DM}, //
					new int[] { 0, 2, 2,0,
						     1,1,1,1,
						     0,2,2,0,0,2,2,0,
						     0,0,0,0,0,0,
						     2,2}//
			);
			setInput(0, 0);
			setOutput(0,1,3);
			
			setInput(1,1);
			setOutput(1,2);

			setInput(2,3);
			setOutput(2, 4);
			
			setInput(3, 2,4 );
			setOutput(3,5);
			
			setInput(4,1,9);
			setOutput(4,6,10);
			
			setInput(5,6,14);
			setOutput(5,2,15);
			
			setInput(6,3,11);
			setOutput(6,7,12);
			
			setInput(7, 7,16);
			setOutput(7,4,17);
			
			setInput(8, 8);
			setOutput(8,9,11);
			
			setInput(9, 9);
			setOutput(9,10);
			
			setInput(10, 11);
			setOutput(10,12);
			
			setInput(11,10,12 );
			setOutput(11,13);
			
			setInput(12, 13 );
			setOutput(12,14,16);
			
			setInput(13, 14);
			setOutput(13,15);
			
			setInput(14, 16);
			setOutput(14,17);
			
			setInput(15, 15,17);
			setOutput(15,18);
			
			setInput(16,1,9,22);
			setOutput(16,10,19);
			
			setInput(17,14,19 );
			setOutput(17, 2,15,23);
			
			setInput(18,1,9,24);
			setOutput(18,10,20);
			
			setInput(19,14,20 );
			setOutput(19, 2,15,25);
			
			setInput(20,3,11,24 );
			setOutput(20, 12, 21);
			
			setInput(21,21,16 );
			setOutput(21, 4,17,25);
			
			setInput(22,22);
			setOutput(22,23);
			
			setInput(23,24);
			setOutput(23,25);
			
			setInitialMarking(0, 8, 22 ,24);
			setFinalMarking(5, 18, 23, 25);
			
		}
	}
	
	public static class SyncProductMultiPerspective5 extends SyncProductImpl {
		public SyncProductMultiPerspective5() {
			super("MultiPerspective_CUDm&CmUmDm", 10,
					new String[] {"AD1_m", "BD2_m",
							      "AsD1_p", "AcD1_p",
							      "AsD1_l","AcD1_l"}, //
					new String[] { "p0", "p1","p2",
							       "p3",
							       "p4", "p5","p6"
							       }, //
					new int[][] { NE, NE,
				                 { 0 }, { 1 }, 
				                 { 0}, { 1}}, //
					new int[] { NR,NR,
			                    0,1,
			                    0,1 }, //
					new int[] { 1, 1, 1, 1, 1,1 }, //
					new byte[] { MM,MM, PM, PM,LM, LM }, //
					new int[] { 2,2,1,1,2,2}//
			);
			setInput(0, 0);
			setOutput(0, 1);
			
			setInput(1, 1);
			setOutput(1, 2);
			
			setInput(2, 0, 4);
			setOutput(2, 3, 5);

			setInput(3, 3, 5);
			setOutput(3, 1, 6);
			
			setInput(4,4 );
			setOutput(4,5 );
			
			setInput(5, 5);
			setOutput(5, 6);
			
			
			setInitialMarking(0, 4);
			setFinalMarking(2, 6);
			
		}
	}
	
	public static class SyncProductMultiPerspective6 extends SyncProductImpl {
		public SyncProductMultiPerspective6() {
			super("MultiPerspective_CUDm&CmUmDm&CnUlDl", 10,
					new String[] {"AD1_m", "BD2_m",
							      "AsD1_p", "AcD1_p",
							      "AsD1_l","AcD1_l",
							      "-,S"}, //
					new String[] { "p0", "p1","p2",
							       "p3",
							       "p4", "p5","p6",
							       "p7","p8"}, //
					new int[][] { NE, NE,
				                 { 0 }, { 1 }, 
				                 { 0}, { 1},
				                 {2}}, //
					new int[] { NR,NR,
			                    0,1,
			                    0,1,
			                    2}, //
					new int[] { 1, 1, 1, 1, 1,1,1 }, //
					new byte[] { MM,MM, PM, PM,LM, LM,DM }, //
					new int[] { 2,2,1,1,2,2,2}//
			);
			setInput(0, 0);
			setOutput(0, 1);
			
			setInput(1, 1);
			setOutput(1, 2);
			
			setInput(2, 0, 4);
			setOutput(2, 3, 5);

			setInput(3, 3, 5);
			setOutput(3, 1, 6);
			
			setInput(4,4 );
			setOutput(4,5 );
			
			setInput(5, 5);
			setOutput(5, 6);
			
			setInput(6, 7);
			setOutput(6, 8);
			
			setInitialMarking(0, 4,7);
			setFinalMarking(2, 6, 8);
			
		}
	}
	public static class SyncProductMultiPerspective7 extends SyncProductImpl {
		public SyncProductMultiPerspective7() {
			super("MultiPerspective_CUDm&CmUmDm&CnUlDl&ClUlDl", 10,
					new String[] {"AD1_m", "BD2_m",
							      "AsD1_p", "AcD1_p",
							      "AsD1_l","AcD1_l","C_l",
							      "-,S"}, //
					new String[] { "p0", "p1","p2",
							       "p3",
							       "p4", "p5","p6","p7",
							       "p8","p9"}, //
					new int[][] { NE, NE,
				                 { 0 }, { 1 }, 
				                 { 0}, { 1},{2},
				                 {3}}, //
					new int[] { NR,NR,
			                    0,1,
			                    0,1,2,
			                    3}, //
					new int[] { 1, 1, 1, 1, 1,1,1,1 }, //
					new byte[] { MM,MM, PM, PM,LM, LM,LM, DM }, //
					new int[] { 2,2,1,1,2,2,2,2}//
			);
			setInput(0, 0);
			setOutput(0, 1);
			
			setInput(1, 1);
			setOutput(1, 2);
			
			setInput(2, 0, 4);
			setOutput(2, 3, 5);

			setInput(3, 3, 5);
			setOutput(3, 1, 6);
			
			setInput(4,4 );
			setOutput(4,5 );
			
			setInput(5, 5);
			setOutput(5, 6);
			
			setInput(6, 6);
			setOutput(6, 7);
			
			setInput(7, 8);
			setOutput(7, 9);
			
			setInitialMarking(0, 4,8);
			setFinalMarking(2, 7, 9);
			
		}
	}
	
	public static class SyncProductMultiPerspective8 extends SyncProductImpl {
		public SyncProductMultiPerspective8() {
			super("MultiPerspective_Parallel", 10,
					new String[] {"T", "AD1_m","BD2_m","T",
							      "AsD1_p", "AcD1_p","BsD2_p", "BcD2_p",
							      "AsD1_l","BsD2_l", "AcD1_l","BcD2_l",
							      "AsD1S1_t", "AcD1S1_t","AsD1S2_t", "AcD1S2_t","BsD2S2_t", "BcD2S2_t",
							      "-,S1","-,S2"}, //
					new String[] { "p0", "p1","p2","p3", "p4", "p5",
							       "p06", "p7",
							       "p8", "p9","p10","p11", "p12",
							       "p13","p14", "p15",
							       "p16","p17", "p18","p19"}, //
					new int[][] { NE,NE,NE,NE,
				                 { 0 }, { 2 },{ 1 }, { 3 }, 
				                 { 0 }, { 1 },{ 2 }, { 3 },
				                 {0},{2},{0},{2},{1},{3},
				                 {4},{5}}, //
					new int[] { NR,NR,NR,NR,
			                 0,2,1,3, 
			                 0,1,2,3,
			                 0,2,0,2,1,3,
			                 4,5}, //
					new int[] { 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}, //
					new byte[] { TM, MM, MM,TM,
							     PM,PM,PM,PM,
							     LM,LM,LM,LM,
							     SM,SM,SM,SM,SM,SM,
							     DM,DM}, //
					new int[] { 0, 2, 2,0,
						     1,1,1,1,
						     2,2,2,2,
						     0,0,0,0,0,0,
						     2,2}//
			);
			setInput(0, 0);
			setOutput(0,1,3);
			
			setInput(1,1);
			setOutput(1,2);

			setInput(2,3);
			setOutput(2, 4);
			
			setInput(3, 2,4 );
			setOutput(3,5);
			
			setInput(4,1,8);
			setOutput(4,6,9);
			
			setInput(5,6,10);
			setOutput(5,2,11);
			
			setInput(6,3,9);
			setOutput(6,10,7);
			
			setInput(7,7,11);
			setOutput(7,4,12);
			
			setInput(8,8);
			setOutput(8,9);
			
			setInput(9,9);
			setOutput(9,10);
			
			setInput(10,10);
			setOutput(10,11);
			
			setInput(11,11);
			setOutput(11,12);
			
			setInput(12,1,8,16);
			setOutput(12,9,13);
			
			setInput(13,10,13);
			setOutput(13,2,11,17);
			
			setInput(14,1,8,18);
			setOutput(14,9,14);
			
			setInput(15,10,14);
			setOutput(15,2,11,19);
			
			setInput(16,3,9,18);
			setOutput(16,10,15);
			
			setInput(17,11,15);
			setOutput(17,4,12,19);
			
			setInput(18,16);
			setOutput(18,17);
			
			setInput(19,18);
			setOutput(19,19);
			
			setInitialMarking(0, 8, 16,18);
			setFinalMarking(5,12, 17,19 );

		}
	}
	*/
	public static class NastySyncProductExample extends SyncProductImpl {

		public NastySyncProductExample() {
			super("Nasty Example", 25,
					new String[] { "A,-", "B,-", "C,-", "D,-", "E,-", "F,-", "G,-", "H,-", "I,-", "J,-", "K,-", "L,-",
							"K,K", "L,L", "-,L", "-,K" }, //
					new String[] { "p0", "p1", "p2", "p3", "p4", "p5", "p6", "p7", "p8", "p9", "p10", "p11", "p12",
							"p13", "p14" }, //
					new int[][] { NE, NE, NE, NE, NE, NE, NE, NE, NE, NE, NE, NE, { 1 }, { 0 }, { 0 }, { 1 } }, //
					new int[] { NR, NR, NR, NR, NR, NR, NR, NR, NR, NR, NR, NR, 1, 0, 0, 1 }, //
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, //
					new byte[] { MM, MM, MM, MM, MM, MM, MM, MM, MM, MM, MM, MM, SM, SM, LM, LM }, //
					//					new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, /**/ 34, 35, /**/ 23, 22 }, //
					new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1 }//
			);
			setInput(0, 0);
			setOutput(0, 1, 2);

			setInput(1, 1);
			setOutput(1, 3);

			setInput(2, 3);
			setOutput(2, 7);

			setInput(3, 1);
			setOutput(3, 4);

			setInput(4, 4);
			setOutput(4, 7);

			setInput(5, 2);
			setOutput(5, 5);

			setInput(6, 5);
			setOutput(6, 8);

			setInput(7, 2);
			setOutput(7, 6);

			setInput(8, 6);
			setOutput(8, 8);

			setInput(9, 7, 8);
			setOutput(9, 9);

			setInput(10, 9);
			setOutput(10, 10);

			setInput(11, 10);
			setOutput(11, 11);

			setInput(12, 9, 13);
			setOutput(12, 10, 14);

			setInput(13, 10, 12);
			setOutput(13, 11, 13);

			setInput(14, 12);
			setOutput(14, 13);

			setInput(15, 13);
			setOutput(15, 14);

			setInitialMarking(0, 12);
			setFinalMarking(11, 14);
		}
	}
   

	
	public static void main(String[] args) throws LPMatrixException {
		// INITIALIZE LpSolve for stdout

	LpSolve.lpSolveVersion();

	int[] alignment;
		SyncProduct net = new SyncProductExampleBook();	
		//SyncProduct net = new NastySyncProductExample();
		//SyncProduct net = new SyncProductMultiPerspective();
		//SyncProduct net = new SyncProductMultiPerspective1();
		//SyncProduct net = new SyncProductMultiPerspective2();
		//SyncProduct net = new SyncProductMultiPerspective3();
	    //SyncProduct net = new SyncProductMultiPerspective4();
	    //SyncProduct net = new SyncProductMultiPerspective5();
	    //SyncProduct net = new SyncProductMultiPerspective6();
	    //SyncProduct net = new SyncProductMultiPerspective7();
	    //SyncProduct net = new SyncProductMultiPerspective8();
		Utils.toDot(net, System.out);

		alignment = testSingleGraph(net, Debug.DOT);
				//Utils.toDot(net, System.out);
		if (alignment != null) {
			Utils.toDot(net, alignment, System.out);
		}
		//		testSingleGraph(new NastySyncProductExample(), Debug.DOT);
	}

	public static int[] testSingleGraph(SyncProduct net, Debug debug) throws LPMatrixException {
		LpSolve.lpSolveVersion();
		
		ReplayAlgorithm algorithm = null;
		//INITIALIZATION OF CLASSLOADER FOR PROPER RECORDING OF TIMES.
		//algorithm = new Dijkstra(net);
	    //algorithm = new AStarLargeLP(net);
		//algorithm = new AStar(net);

		boolean dijkstra = false;
		boolean split = true;
		boolean moveSort = true; // moveSort on total order
		boolean queueSort = true; // queue sorted "depth-first"
		boolean preferExact = true; // prefer Exact solution
		//		int multiThread = 1; // do multithreading
		boolean useInt = false; //  use Integer

	 if (split) {
			System.out.println("**********Split");
			algorithm = new AStarLargeLP(net, //
					moveSort, // moveSort on total order
					useInt, // use Integers
					debug // debug mode
			);

		} 

		try {
			return algorithm.run(Progress.INVISIBLE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
		} catch (LPMatrixException e) {
			e.printStackTrace();
		}
		return null;
		//		for (int t : alignment) {
		//			System.out.println(net.getTransitionLabel(t));
		//		}
	}

}
