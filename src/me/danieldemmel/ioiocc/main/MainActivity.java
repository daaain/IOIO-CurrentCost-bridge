package me.danieldemmel.ioiocc.main;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import me.danieldemmel.ioiocc.utility.Utilities;

import org.apache.http.util.EncodingUtils;

import ioio.examples.simple.R;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.Uart;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.AbstractIOIOActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends AbstractIOIOActivity
{
	// convert ASCII equivalent of "<msg>" (with which the transmission should start) to bytes
	private static final byte[] messageStart = EncodingUtils.getAsciiBytes("<msg>");

	private TextView uartTextView_;
	private TextView bufferTextView_;
	private TextView debugTextView_;
	private ToggleButton toggleButton_;
	private byte[] readBuffer = new byte[32768];
	private int bufferOffset = 0;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		bufferTextView_ = (TextView)findViewById(R.id.BufferTextView);
		uartTextView_ = (TextView)findViewById(R.id.UartTextView);
		debugTextView_ = (TextView)findViewById(R.id.DebugTextView);
		toggleButton_ = (ToggleButton)findViewById(R.id.ToggleButton);

		enableUi(false);
	}

	class IOIOThread extends AbstractIOIOActivity.IOIOThread
	{
		private DigitalOutput led_;
		private Uart uart_;
		private InputStream in_;

		@Override
		public void setup() throws ConnectionLostException
		{
			try
			{
				led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
				
				// The CC128 Display Unit outputs ASCII text over its serial port at 57600 baud,
				// 1 start, 8-bit data, 1 stop, no parity, no handshake.

				// Uart uart = ioio.openUart(rxPin, txPin, baud, parity, stopBits);
				uart_ = ioio_.openUart(7, IOIO.INVALID_PIN, 57600, Uart.Parity.NONE, Uart.StopBits.ONE);
				in_ = uart_.getInputStream();
				enableUi(true);
			}
			catch (ConnectionLostException e)
			{
				enableUi(false);
				throw e;
			}
		}

		@Override
		public void loop() throws ConnectionLostException
		{
			try
			{
				led_.write(!toggleButton_.isChecked());
				
				int availableBytes = 0;
				try
				{
					availableBytes = in_.available();
					setBufferText("Offset " + bufferOffset + "| Buffer " + availableBytes);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

				if (availableBytes > 0 && bufferOffset < 30000)
				{               
					try
					{ 
						in_.read(readBuffer, bufferOffset, availableBytes);
					} 
					catch (IOException e) 
					{ 
						e.printStackTrace(); 
					}
					
					bufferOffset += availableBytes;
					
//					int messageStartIndex = Utilities.KMPMatch.indexOf(readBuffer, messageStart);
//
//					if(messageStartIndex != -1)
//					{
//						setDebugText("Found <msg>!");
//					}

//					String bufferValueHex = Utilities.toHex(readBuffer);
//					// trim empty bytes from buffer
//					bufferValueHex = bufferValueHex.substring(0, bufferOffset);
					
					String bufferValueAscii = "";
					
					try
					{
						bufferValueAscii = new String(readBuffer, "US-ASCII");
						bufferValueAscii = bufferValueAscii.substring(0, bufferOffset);
					}
					catch (UnsupportedEncodingException e)
					{
						e.printStackTrace();
					}
					 
					//setUartText("Hex: " + bufferValueHex + "\n________________________\nUS-ASCII: " + bufferValueAscii);
					setUartText(bufferValueAscii);
				}

				sleep(500);
			}
			catch (InterruptedException e)
			{
				ioio_.disconnect();
			}
			catch (ConnectionLostException e)
			{
				enableUi(false);
				throw e;
			}
		}
	}

	@Override
	protected AbstractIOIOActivity.IOIOThread createIOIOThread()
	{
		return new IOIOThread();
	}

	private void enableUi(final boolean enable)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				toggleButton_.setEnabled(enable);
			}
		});
	}

	private void setBufferText(final String str)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				bufferTextView_.setText(str);
			}
		});
	}

//	private void setDebugText(final String str)
//	{
//		runOnUiThread(new Runnable()
//		{
//			@Override
//			public void run()
//			{
//				debugTextView_.setText(str);
//			}
//		});
//	}

	private void setUartText(final String str)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				uartTextView_.setText(str);
			}
		});
	}
}