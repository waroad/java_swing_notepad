package projectTTS;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;
import javax.swing.*;
import javax.swing.border.Border;

import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1.StreamingRecognitionResult;
import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;

public class MyFrame extends JFrame{
	static String str;
	BufferedReader br = new BufferedReader(new FileReader("word.txt"));
	static Object[][][] SubjectWord = new Object[5][][];
	JButton[] Subjects = new JButton[5];
	JButton[] deleteSubjects = new JButton[5];
	int cntSubject=1,count;
	static int currentSubject;
	String tempSubjectName;
	String line;
	Object[] columnNames = {"중요도","단어","뜻","선택"};
	Object[] voice = {"확인","취소","음성인식"};
	JTable[] wordTable = new JTable[5];
	static int[] wordColor= {0x7aa7f0,0x629af5,0x4a89f0,0x3a7ff0,0x2471ed};
	JScrollPane[] scroll_table = new JScrollPane[5];
	static int[] SubjectWordCnt= {0,0,0,0,0};
	static ImageIcon icon2 = new ImageIcon("noteicon.png");
	MyFrame() throws IOException{
		ImageIcon iconX = new ImageIcon("XX.png");
		ImageIcon firstPage = new ImageIcon("hello.png");
		ImageIcon mainPage = new ImageIcon("categories.png");
		ImageIcon subPage = new ImageIcon("subject.png");
		ImageIcon iconStar = new ImageIcon("ic_star.png");
		JButton startButton = new JButton("시작");  		//처음 start 및 뒤로 가는 버튼
		JButton addSubjectButton = new JButton("+"); 	//주제 추가 버튼
		JButton testButton = new JButton("TEST");    	//해당 과목에 있는 것들 테스트 버튼
		JButton deleteSelectedWords = new JButton("");	//해당 주제 삭제 버튼
		JButton addWord = new JButton("");         		//해당 주제 테이블에 새로운 단어 추가 버튼
		JPanel selectBox= new JPanel();	
		JLabel selectYN=new JLabel();
		JLabel backGroundImage = new JLabel();			//JLabel for 과목 선택 화면 background
		JCheckBox selectAll = new JCheckBox(); 			//전체 체크박스 
		JTextField subjectText = new JTextField();     	//주제 추가할 때 쓰는 텍스트필드
		
		
		//각 주제 별 단어 저장용 배열 생성
		for(int i=0;i<5;i++) 
			SubjectWord[i]=new Object[100][4];
		
		//각 주제를 메인 화면에서 고를 수 있는 버튼 생성
		for(int i=0;i<5;i++) {
			Subjects[i] = new JButton();
			Subjects[i].setVisible(false);
			Subjects[i].setBackground(new Color(wordColor[i]));
			Subjects[i].setFont(new Font("Comic Sans", Font.BOLD, 30));
			Subjects[i].setBounds(50,250+(i)*102,358,84);
			backGroundImage.add(Subjects[i]);
			deleteSubjects[i]=new JButton();
			deleteSubjects[i].setVisible(false);
			deleteSubjects[i].setBackground(new Color(wordColor[i]));
			deleteSubjects[i].setIcon(iconX);
			deleteSubjects[i].setBounds(410,250+(i)*102,30,30);
			backGroundImage.add(deleteSubjects[i]);
		}
		
		


		//Subjects 눌렀을 때 보여지는 스크롤 가능한 테이블 생성
		for(int i=0;i<5;i++) {
			wordTable[i] = new JTable(SubjectWord[i],columnNames) {
				@Override
				public Class getColumnClass(int column) {
	                switch (column) {
		                case 0:
		                	return Icon.class;
	                    case 1:
	                        return String.class;
	                    case 2:
	                        return String.class;
	                    default:
	                        return Boolean.class;
	                }
	            }
				@Override
				public boolean isCellEditable(int row, int col) {
					if(SubjectWordCnt[currentSubject]>row) {
						return (col==3);
					}
			        return col==4;
			    }
			};
			int k=i;
			wordTable[i].addMouseListener(new java.awt.event.MouseAdapter() {
			    @Override
			    public void mouseClicked(java.awt.event.MouseEvent evt) {
			        int row = wordTable[k].rowAtPoint(evt.getPoint());
			        int col = wordTable[k].columnAtPoint(evt.getPoint());
			        if (row<SubjectWordCnt[k]) {
				        if (col == 0 && SubjectWord[k][row][0]==Boolean.FALSE) {
				        	SubjectWord[k][row][0]="star.jpg";
							wordTable[k].setValueAt(iconStar, row, 0);
				        }
				        else if(col == 0) {
				        	SubjectWord[k][row][0]=Boolean.FALSE;
							wordTable[k].setValueAt(Boolean.FALSE, row, 0);
				        }
			        }
			    }
			});
			wordTable[i].setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
			wordTable[i].setFont(new Font("돋움체", Font.BOLD, 15));
			wordTable[i].setBackground(new Color(wordColor[0]));
			wordTable[i].setRowHeight(40);
			scroll_table[i] = new JScrollPane(wordTable[i]);
			scroll_table[i].setVisible(false);
			scroll_table[i].setBounds(43,266,394,502);
			wordTable[i].getColumnModel().getColumn(0).setPreferredWidth(50);
			wordTable[i].getColumnModel().getColumn(1).setPreferredWidth(150);
			wordTable[i].getColumnModel().getColumn(2).setPreferredWidth(150);
			wordTable[i].getColumnModel().getColumn(3).setPreferredWidth(50);
			backGroundImage.add(scroll_table[i]);
		}

		//word.txt파일 읽어서 배열 안에 저장.
		int subjectcnt=0;
		line = br.readLine();
		count = Integer.parseInt(line);
		cntSubject=count;
		for(int i=0;i<count;i++) 
			Subjects[i].setText(br.readLine());
		while(true) {
		 	line = br.readLine();
            if (line==null) break;
            count = Integer.parseInt(line);
		 	for(int i=0;i<count;i++) {
	            line = br.readLine();
	            String[] wordline = line.split(" ");
	            if(wordline[0].equals("ic_star.png")) {
	            	SubjectWord[subjectcnt][i][0]="ic_star.png";
	            	wordTable[subjectcnt].setValueAt(iconStar, i, 0);
	            }
	            else SubjectWord[subjectcnt][i][0]=Boolean.FALSE;
	            SubjectWord[subjectcnt][i][1]=wordline[1];
	            SubjectWord[subjectcnt][i][2]=wordline[2];
	            SubjectWord[subjectcnt][i][3]=Boolean.FALSE;
		 	}
			SubjectWordCnt[subjectcnt]+=count;
		 	subjectcnt++;
        }
        br.close();
		//주제 추가할 때 주제 이름 정하는 칸
		subjectText.setVisible(false);
		subjectText.setFont(new Font("Comic Sans", Font.BOLD, 20));

		//시험 응시 버튼 시각적 속성 설정
		testButton.setFont(new Font("Comic Sans", Font.BOLD, 30));
		testButton.setVisible(false);
		testButton.setBounds(254,50,180,48);
		testButton.setBorderPainted(false);		//make the button transparent
		testButton.setFont(new Font("Arial", Font.BOLD, 30));
		testButton.setForeground(Color.WHITE);	//폰트 색 설정
		testButton.setOpaque(false);
		testButton.setFocusPainted(false);
		testButton.setBorderPainted(false);
		testButton.setContentAreaFilled(false);
		
		//단어 전체 선택, 해제하는 체크박스
		selectAll.setHorizontalAlignment(JCheckBox.LEFT);
		selectAll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		selectYN.setText("전체 선택");
		selectBox.add(selectYN);
		selectBox.add(selectAll);
		selectBox.setVisible(false);
		selectBox.setBackground(new Color(wordColor[0]));
		selectBox.setBounds(340,204,94,33);
		selectBox.setOpaque(false);

		//단어 삭제 버튼 시각적 속성
		deleteSelectedWords.setVisible(false);
		deleteSelectedWords.setBounds(114,197,42,43);
		deleteSelectedWords.setOpaque(false);
		deleteSelectedWords.setFocusPainted(false);
		deleteSelectedWords.setContentAreaFilled(false);
		
		//단어 추가 버튼 시각적 속성
		addWord.setVisible(false);
		addWord.setBounds(47,197,42,43);
		addWord.setOpaque(false);
		addWord.setFocusPainted(false);
		addWord.setContentAreaFilled(false);
		
		//주제 추가 버튼 시각적 속성
		addSubjectButton.setBounds(50,250+(cntSubject)*102,358,84);
		addSubjectButton.setFont(new Font("Comic Sans", Font.BOLD, 50));
		addSubjectButton.setVisible(false);
		addSubjectButton.setBackground(new Color(0x14A989));
		
		//맨 처음 start 겸 main화면으로 가는 뒤로가기 버튼
		startButton.setBounds(150,520,180,180);
		startButton.setFocusable(false);
		startButton.setVerticalTextPosition(JButton.BOTTOM);
		startButton.setFont(new Font("Comic Sans", Font.BOLD, 30));
		startButton.setBackground(new Color(0xFFFFFF));
		startButton.setForeground(Color.WHITE);
		startButton.setOpaque(false);
		startButton.setFocusPainted(false);
		startButton.setBorderPainted(false);
		startButton.setContentAreaFilled(false);

		//새로운 주제 추가하기 버튼을 눌렀을 때, 주제명을 적을 수 있는 TextField 생성
		addSubjectButton.addActionListener(e -> {
			addSubjectButton.setVisible(false);
			for(int i=0;i<cntSubject;i++) {
				Subjects[i].setEnabled(false);
				deleteSubjects[i].setEnabled(false);
			}
			if(cntSubject<5) {
				Subjects[cntSubject].setVisible(false);
				subjectText.setBounds(50,250+(cntSubject)*102,358,84);
				subjectText.setVisible(true);
				if(cntSubject==4) //5개가 다 차면 더 이상 추가 못하게 막음
					addSubjectButton.setVisible(false);
			}
		});
		
		//TextField에 추가 할 주제의 이름을 입력받았을 때, 그 이름을 가진 주제 버튼 만들기
		subjectText.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		    	if(subjectText.getText().equals("")) {
		    		;
		    	}
		    	else {
		    		cntSubject++;
		    		tempSubjectName=subjectText.getText();
			    	Subjects[cntSubject-1].setVisible(true);
			    	Subjects[cntSubject-1].setText(tempSubjectName);
			    	deleteSubjects[cntSubject-1].setVisible(true);
			    	subjectText.setText("");
					addSubjectButton.setBounds(50,250+(cntSubject)*102,358,84);
		    	}
		    	if(cntSubject!=5)
		    		addSubjectButton.setVisible(true);
				subjectText.setVisible(false);
				for(int i=0;i<cntSubject;i++) {
					Subjects[i].setEnabled(true);
					deleteSubjects[i].setEnabled(true);
				}
		    }
		});
		
		for(int i=0;i<5;i++) {
			int k=i;
			
			//각 주제인 Subjects[i]가 눌렸을 때 해당 주제 안에 있는 단어 Table 보여주기
			Subjects[i].addActionListener(e -> {
				currentSubject=k;
				backGroundImage.setIcon(subPage); //배경화면 이미지 교체
				startButton.setVisible(true);
				testButton.setVisible(true);
				deleteSelectedWords.setVisible(true);
				addWord.setVisible(true);
				selectBox.setVisible(true);
				scroll_table[currentSubject].setVisible(true);
				for(int j=0;j<5;j++) {
					Subjects[j].setVisible(false);
					deleteSubjects[j].setVisible(false);
				}
				addSubjectButton.setVisible(false);
			});
			
			//옆에 있는 조그마한 꼽표로 해당 주제를 다 지울 때
			deleteSubjects[i].addActionListener(e -> { 
				currentSubject=k;
				int a=JOptionPane.showConfirmDialog(this,"해당 주제 및 주제 안에 있는 단어를 모두 지우시겠습니까?"); 
				if(a==JOptionPane.YES_OPTION){  
					for(int l=currentSubject;l<4;l++) {
						Subjects[l].setText(Subjects[l+1].getText());
						for(int j=0;j<Math.max(SubjectWordCnt[l],SubjectWordCnt[l+1]);j++) {
							SubjectWord[l][j]=SubjectWord[l+1][j];
						}
					}
					cntSubject-=1;
					Subjects[cntSubject].setVisible(false);
					deleteSubjects[cntSubject].setVisible(false);
					SubjectWordCnt[cntSubject]=0;
					addSubjectButton.setBounds(50,250+(cntSubject)*102,358,84);
					if(cntSubject<5)
						addSubjectButton.setVisible(true);
				}
			});
		}
				
		//테스트 버튼
		testButton.addActionListener(e -> {
			for(int i=0;i<SubjectWordCnt[currentSubject];i++) {
				if((Boolean)SubjectWord[currentSubject][i][3]==true) {
					try {
						new test();
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (LineUnavailableException e1) {
						e1.printStackTrace();
					} catch (UnsupportedAudioFileException e1) {
						e1.printStackTrace();
					}
					break;
				}
				else if(i==SubjectWordCnt[currentSubject]-1) {
					JOptionPane.showMessageDialog(this,"테스트를 할 단어를 선택해주세요."); 
				}
			}
		});

		//단어 전체 선택 체크박스
		selectAll.addActionListener(e -> {
			for(int i=0;i<SubjectWordCnt[currentSubject];i++) {
				if(selectAll.isSelected())
					SubjectWord[currentSubject][i][3]=true;
				else
					SubjectWord[currentSubject][i][3]=false;
			}
			scroll_table[currentSubject].setVisible(false);
			scroll_table[currentSubject].setVisible(true);
		});
		
		//체크된 단어 삭제하기
		deleteSelectedWords.addActionListener(e -> {
			int cnt=0;
			int tmp = SubjectWordCnt[currentSubject];
			for(int i=0;i<tmp;i++) {
				while(cnt< tmp&& (boolean)SubjectWord[currentSubject][cnt][3]==true ) {
					SubjectWordCnt[currentSubject]-=1;
					cnt++;
				}
				SubjectWord[currentSubject][i][0]=SubjectWord[currentSubject][cnt][0];
				SubjectWord[currentSubject][i][1]=SubjectWord[currentSubject][cnt][1];
				SubjectWord[currentSubject][i][2]=SubjectWord[currentSubject][cnt][2];
				SubjectWord[currentSubject][i][3]=SubjectWord[currentSubject][cnt][3];
				cnt++;
			}
			if (cnt==SubjectWordCnt[currentSubject]) {
				JOptionPane.showMessageDialog(this,"삭제할 단어를 선택해주세요."); 
			}
			scroll_table[currentSubject].setVisible(false);
			scroll_table[currentSubject].setVisible(true);
		});

		//단어 추가 버튼
		addWord.addActionListener(e -> {
			JTextField WordField = new JTextField(10);
			JTextField MeanField = new JTextField(10);

			JPanel myPanel = new JPanel();
			myPanel.add(new JLabel("단어:"));
			myPanel.add(WordField);
			myPanel.add(Box.createHorizontalStrut(20));
			myPanel.add(new JLabel("뜻:"));
			myPanel.add(MeanField);
			int result = JOptionPane.showOptionDialog(null, myPanel, "입력할 단어와 뜻 입력", JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE, iconStar, voice, null);

			if (result == JOptionPane.OK_OPTION) {
				String Word = WordField.getText();
				String Mean = MeanField.getText();
				if(Word.equals("") || Mean.equals(""))
					JOptionPane.showMessageDialog(this,"제대로 입력해 주세요."); 
				else {
					SubjectWord[currentSubject][SubjectWordCnt[currentSubject]][0]=Boolean.FALSE;
					SubjectWord[currentSubject][SubjectWordCnt[currentSubject]][1]=Word;
					SubjectWord[currentSubject][SubjectWordCnt[currentSubject]][2]=Mean;
					SubjectWord[currentSubject][SubjectWordCnt[currentSubject]++][3]=Boolean.FALSE;
					scroll_table[currentSubject].setVisible(false);
					scroll_table[currentSubject].setVisible(true);
				}
			}
		  	if (result == 2) { // 음성인식이 골라졌을 때
		  		SubjectWord[currentSubject][SubjectWordCnt[currentSubject]][0]=Boolean.FALSE;
		  		str=null;
		  		String tmp1=null;
		  		String tmp2=null;
		  		int tmp3=-1;
		  		JOptionPane.showMessageDialog(myPanel, "OK를 눌러 단어 음성인식 시작", null, 1);
				try {
					while(str==null) {
						streamingMicRecognize(0);
						if(str==null) tmp3= JOptionPane.showConfirmDialog(this, "제대로 인식이 안됐습니다. 다시 하시겠습니까?"); 
						else tmp3=0;
						if(tmp3!=0) return;
					}
					tmp1=str;
				} catch (Exception e1) {
				    e1.printStackTrace();
				}
				str=null;
				JOptionPane.showMessageDialog(myPanel, "OK를 눌러 뜻(한국어) 음성인식 시작", null, 1);
				try {
					while(str==null) {
						streamingMicRecognize(0);
						if(str==null) tmp3= JOptionPane.showConfirmDialog(this, "제대로 인식이 안됐습니다. 다시 하시겠습니까?"); 
						else tmp3=0;
						if(tmp3!=0) return;
					}
					tmp2=str;
				} catch (Exception e1) {
				    e1.printStackTrace();
				}
		  		SubjectWord[currentSubject][SubjectWordCnt[currentSubject]][0]=Boolean.FALSE;
				SubjectWord[currentSubject][SubjectWordCnt[currentSubject]][1]=tmp1;
				SubjectWord[currentSubject][SubjectWordCnt[currentSubject]][2]=tmp2;
				SubjectWord[currentSubject][SubjectWordCnt[currentSubject]++][3]=Boolean.FALSE;
				scroll_table[currentSubject].setVisible(false);
				scroll_table[currentSubject].setVisible(true);
			}
		});
		
		//메인화면으로 가기
		startButton.addActionListener(e -> {
			backGroundImage.setIcon(mainPage);
			startButton.setBounds(41,41,64,64);
			startButton.setBorderPainted(false);	
			startButton.setBorderPainted(true);
			startButton.setText("");
			startButton.setVisible(false);
			testButton.setVisible(false);
			deleteSelectedWords.setVisible(false);
			addWord.setVisible(false);
			selectBox.setVisible(false);
			selectAll.setSelected(false);
			backGroundImage.setVisible(true);
			for(int i=0;i<cntSubject;i++) {
				scroll_table[i].setVisible(false);
				Subjects[i].setVisible(true);
				deleteSubjects[i].setVisible(true);
			}
			if(cntSubject<5)
				addSubjectButton.setVisible(true);
		});

        

		this.setIconImage(icon2.getImage());
        this.setTitle("Fancy Note Pad");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(480,853);	//창 크기 설정, 16:9 종횡비
        this.setLayout(null);
        this.setVisible(true);
		this.add(backGroundImage);

        backGroundImage.add(addSubjectButton);
        backGroundImage.add(deleteSelectedWords);
        backGroundImage.add(subjectText);
        backGroundImage.add(testButton);
        backGroundImage.add(addWord);
        backGroundImage.add(selectBox);
        backGroundImage.add(startButton);
		backGroundImage.setBounds(-8,-10,480,853);
        backGroundImage.setIcon(firstPage);
		backGroundImage.setVisible(true);
		
        //창 닫을 때 saveFile()을 호출하면서 때 배열 안에 있는 값들을 text 파일에 저장
        this.addWindowListener(new java.awt.event.WindowAdapter() {
	        @Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent){
	        	try {
	        		saveFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.exit(0);
			}
        });
	}
	void saveFile() throws IOException {
    	FileWriter fw = new FileWriter("word.txt");
		fw.write(cntSubject+"\n");
		for(int i=0;i<cntSubject;i++) {
		 	fw.write(Subjects[i].getText()+"\n");
		}
		for(int i=0;i<cntSubject;i++) {
			fw.write(SubjectWordCnt[i]+"\n");
			for(int j=0;j<SubjectWordCnt[i];j++) {
				fw.write(SubjectWord[i][j][0]+" "+SubjectWord[i][j][1]+" "+SubjectWord[i][j][2]+"\n");
			}
		}
		fw.close();
    }
	//음성인식 후 String 안에 인식 값 넣기
	public static void streamingMicRecognize(int lan) throws Exception {
    	ResponseObserver<StreamingRecognizeResponse> responseObserver = null;
        try (SpeechClient client = SpeechClient.create()) {
            responseObserver =
                new ResponseObserver<StreamingRecognizeResponse>() {
                    ArrayList<StreamingRecognizeResponse> responses = new ArrayList<>();

                    public void onStart(StreamController controller) {}

                    public void onResponse(StreamingRecognizeResponse response) {
                        responses.add(response);
                    }

                    public void onComplete() {
                        for (StreamingRecognizeResponse response : responses) {
                            StreamingRecognitionResult result = response.getResultsList().get(0);
                            SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                            System.out.printf("Transcript : %s\n", alternative.getTranscript());
                            str=alternative.getTranscript();
                        }
                    }

                    public void onError(Throwable t) {
                        System.out.println(t);
                    }
                };

	        ClientStream<StreamingRecognizeRequest> clientStream =
	                client.streamingRecognizeCallable().splitCall(responseObserver);


	        RecognitionConfig recognitionConfig;
	        if(lan==0) {
		        recognitionConfig=RecognitionConfig.newBuilder()
		                        .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
		                        .setLanguageCode("en-US")
		                        .setSampleRateHertz(16000)
		                        .build();
	        }
	        else {
		        recognitionConfig=RecognitionConfig.newBuilder()
		                        .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
		                        .setLanguageCode("ko-KR")
		                        .setSampleRateHertz(16000)
		                        .build();
	        }
	        StreamingRecognitionConfig streamingRecognitionConfig =
	                StreamingRecognitionConfig.newBuilder().setConfig(recognitionConfig).build();

	        StreamingRecognizeRequest request =
	                StreamingRecognizeRequest.newBuilder()
	                        .setStreamingConfig(streamingRecognitionConfig)
	                        .build(); // The first request in a streaming call has to be a config

	        clientStream.send(request);
	        // SampleRate:16000Hz, SampleSizeInBits: 16, Number of channels: 1, Signed: true,
	        // bigEndian: false
	        AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);
	        DataLine.Info targetInfo =
	                new Info(
	                        TargetDataLine.class,
	                        audioFormat); // Set the system information to read from the microphone audio stream

	        if (!AudioSystem.isLineSupported(targetInfo)) {
	            System.out.println("Microphone not supported");
	            System.exit(0);
	        }
	        // Target data line captures the audio stream the microphone produces.
	        TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
	        targetDataLine.open(audioFormat);
	        targetDataLine.start();
	        System.out.println("Start speaking");
	        long startTime = System.currentTimeMillis();
	        // Audio Input Stream
	        AudioInputStream audio = new AudioInputStream(targetDataLine);
	        while (true) {
	            long estimatedTime = System.currentTimeMillis() - startTime;
	            byte[] data = new byte[6400];
	            audio.read(data);
	            if (estimatedTime > 3000) { // 6 seconds
	                System.out.println("Stop speaking.");
	                targetDataLine.stop();
	                targetDataLine.close();
	                break;
	            }
	            request = StreamingRecognizeRequest.newBuilder().setAudioContent(ByteString.copyFrom(data)).build();
	            clientStream.send(request);
	        }
	    } catch (Exception e) {
	        System.out.println(e);
	    }
	    responseObserver.onComplete();
	}
} 
