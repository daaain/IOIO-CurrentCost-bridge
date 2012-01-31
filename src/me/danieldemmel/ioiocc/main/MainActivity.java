package me.danieldemmel.ioiocc.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import ioio.examples.simple.R;
import ioio.lib.api.Uart;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.AbstractIOIOActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AbstractIOIOActivity
{
	private TextView uartTextView_;
	private TextView bufferTextView_;
	private char[] readBuffer = new char[10000];
	private int bufferOffset = 0;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		bufferTextView_ = (TextView)findViewById(R.id.BufferTextView);
		uartTextView_ = (TextView)findViewById(R.id.UartTextView);
	}

	class IOIOThread extends AbstractIOIOActivity.IOIOThread
	{
		private Uart uart_;
		private InputStream in_;
		private BufferedReader bufRd_;
		
		@Override
		public void setup() throws ConnectionLostException
		{
			try
			{
				// The CC128 Display Unit outputs ASCII text over its serial port at 57600 baud,
				// 1 start, 8-bit data, 1 stop, no parity, no handshake.

				// Uart uart = ioio.openUart(rxPin, txPin, baud, parity, stopBits);
				uart_ = ioio_.openUart(7, IOIO.INVALID_PIN, 57600, Uart.Parity.NONE, Uart.StopBits.ONE);
				in_ = uart_.getInputStream();
				try
				{
					bufRd_ = new BufferedReader(new InputStreamReader(in_, "US-ASCII"));
				}
				catch (UnsupportedEncodingException e)
				{
					e.printStackTrace();
				}				
			}
			catch (ConnectionLostException e)
			{
				throw e;
			}
		}

		@Override
		public void loop() throws ConnectionLostException
		{
			try
			{
				int availableBytes = 0;
				try
				{
					availableBytes = in_.available();
					setBufferText("Offset " + bufferOffset + "| Buffer " + availableBytes);
					Log.v("IOIO-CC-Bridge","Offset " + bufferOffset + "| Buffer " + availableBytes);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

				if (availableBytes > 0 && bufferOffset + availableBytes < readBuffer.length)
				{               
					try
					{ 
						 
						bufRd_.read(readBuffer, bufferOffset, availableBytes);
					} 
					catch (IOException e) 
					{ 
						e.printStackTrace(); 
					}
					
					bufferOffset += availableBytes;
					 
					setUartText(new String(readBuffer));
				} else if(bufferOffset + availableBytes > 8000) {
					ioio_.disconnect();
				}

				sleep(50);
			}
			catch (InterruptedException e)
			{
				ioio_.disconnect();
			}
		}
	}

	@Override
	protected AbstractIOIOActivity.IOIOThread createIOIOThread()
	{
		return new IOIOThread();
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