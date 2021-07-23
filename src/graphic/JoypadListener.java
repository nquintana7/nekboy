package graphic;

import cpu.Bits;
import cpu.InterruptsController;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class JoypadListener implements KeyListener {
    private Joypad joy;

    public JoypadListener (Joypad j) {
        joy = j;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case(KeyEvent.VK_X): {
                joy.writePad_State(1, false, 0);
                //    joy.ic.requestInterrupt(4);
                break;
            }
            case(KeyEvent.VK_C): {
                joy.writePad_State(1, false, 1);
                 //   joy.ic.requestInterrupt(4);
                break;
            }
            case(KeyEvent.VK_ENTER): {
                joy.writePad_State(1, false, 3);
                //joy.ic.requestInterrupt(4);
                break;
            }
            case(KeyEvent.VK_UP): {
                joy.writePad_State(0, false, 2);
                 //   joy.ic.requestInterrupt(4);
                break;
            }
            case(KeyEvent.VK_LEFT): {
                joy.writePad_State(0, false, 1);
                 //   joy.ic.requestInterrupt(4);
                break;
            }
            case(KeyEvent.VK_RIGHT): {
                joy.writePad_State(0, false, 0);
                    //joy.ic.requestInterrupt(4);
                break;
            }
            case(KeyEvent.VK_DOWN): {
                joy.writePad_State(0, false, 3);
                    //joy.ic.requestInterrupt(4);
                break;
            }
            default:
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case(KeyEvent.VK_X): {
                joy.writePad_State(1, true, 0);
                break;
            }
            case(KeyEvent.VK_C): {
                joy.writePad_State(1, true, 1);
                break;
            }
            case(KeyEvent.VK_ENTER): {
                joy.writePad_State(1, true, 3);
                break;
            }
            case(KeyEvent.VK_UP): {
                joy.writePad_State(0, true, 2);
                break;
            }
            case(KeyEvent.VK_LEFT): {
                joy.writePad_State(0, true, 1);
                break;
            }
            case(KeyEvent.VK_RIGHT): {
                joy.writePad_State(0, true, 0);
                break;
            }
            case(KeyEvent.VK_DOWN): {
                joy.writePad_State(0, true, 3);
                break;
            }
            default:
        }
    }
}
