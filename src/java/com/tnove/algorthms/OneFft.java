package com.tnove.algorthms;

/**
 * ���㸵bҶ�任��FFT��
 * @author ruibo
 *
 */
public class OneFft
{

	/**
	 * 
	 */
	// ��bҶ�任����
	int count;
	// ѭ����
	int i, j, k;
	// �м��
	int bfsize, p;

	int power;//��������Ĵ���

	Complex[] w, x1, x2, x;
	Complex[] fd;

	/**
	 * 
	 * @param data
	 * @param power
	 */
	public void setData(Complex[] data, int power)
	{

		this.power = power;

		// �Ƕ�
		double angle;
       // ���㸵bҶ�任�ĵ���
		count = 1 << power;

		// ����ռ�
		w = new Complex[count / 2];
		x = new Complex[count];
		x1 = new Complex[count];
		x2 = new Complex[count];
		fd = new Complex[count];

		// ��ʼ��
		for (i = 0; i < count / 2; i++)
		{
			w[i] = new Complex();
		}
		for (i = 0; i < count; i++)
		{
			x[i] = new Complex();
			x1[i] = new Complex();
			x2[i] = new Complex();
			fd[i] = new Complex();
		}

		// �����Ȩϵ��
		for (i = 0; i < count / 2; i++)
		{
			angle = -i * Math.PI * 2 / count;
			w[i].re = Math.cos(angle);
			w[i].im = Math.sin(angle);
		}

		// ��ʵ���д��x1
		for (i = 0; i < count; i++)
		{
			x1[i] = data[i];

		}
	}

	/**
	 * ���δ���
	 * 
	 * @return
	 */
	public Complex[] getData()
	{

		// ��������
		for (k = 0; k < power; k++)
		{
			for (j = 0; j < 1 << k; j++)
			{
				bfsize = 1 << (power - k);
				for (i = 0; i < bfsize / 2; i++)
				{
					Complex temp1 = new Complex(0, 0);
					Complex temp2 = new Complex(0, 0);

					p = j * bfsize;
					x2[i + p] = temp1.Add(x1[i + p], x1[i + p + bfsize / 2]);

					temp2 = temp1.Sub(x1[i + p], x1[i + p + bfsize / 2]);

					x2[i + p + bfsize / 2] = temp1.Mul(temp2, w[i * (1 << k)]);
				}
			}
			x = x1;
			x1 = x2;
			x2 = x;
		}

		// ��������
		for (j = 0; j < count; j++)
		{
			p = 0;
			for (i = 0; i < power; i++)
			{
				if ((j & (1 << i)) != 0)
					p += 1 << (power - i - 1);
			}
			fd[j] = x1[p];
		}
		return fd;
	}
}