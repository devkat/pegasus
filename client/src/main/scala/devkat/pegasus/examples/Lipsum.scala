package devkat.pegasus.examples

import devkat.pegasus.model.{CharacterStyle, ParagraphStyle}
import devkat.pegasus.model.nested.{Character, Flow, Paragraph, Span}
import shapeless.HMap

object Lipsum {

  val lipsum: String =
    """One morning, when Gregor Samsa woke from troubled dreams, he found himself transformed in his bed into a horrible vermin. He lay on his armour-like back, and if he lifted his head a little he could see his brown belly, slightly domed and divided by arches into stiff sections.
      |The bedding was hardly able to cover it and seemed ready to slide off any moment. His many legs, pitifully thin compared with the size of the rest of him, waved about helplessly as he looked. "What's happened to me?" he thought. It wasn't a dream. His room, a proper human room although a little too small, lay peacefully between its four familiar walls. A collection of textile samples lay spread out on the table - Samsa was a travelling salesman - and above it there hung a picture that he had recently cut out of an illustrated magazine and housed in a nice, gilded frame. It showed a lady fitted out with a fur hat and fur boa who sat upright, raising a heavy fur muff that covered the whole of her lower arm towards the viewer. Gregor then turned to look out the window at the dull weather.
      |""".stripMargin

  val lipsum2: String =
    """Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent varius viverra suscipit. Quisque tristique, dui ac pretium sagittis, metus odio condimentum metus, et accumsan purus tortor non mi. Proin iaculis tristique malesuada. Pellentesque ac justo non leo venenatis pharetra. Proin dictum neque eros, quis sagittis dolor venenatis facilisis. Aliquam pulvinar eros dolor, vel pellentesque nunc tristique et. Integer ac nunc placerat lectus aliquet pellentesque ac eu risus. Quisque suscipit urna lacinia purus vehicula, at rutrum neque vulputate. Curabitur placerat eleifend quam quis laoreet. Pellentesque id placerat justo, consectetur pharetra enim. Integer leo sapien, vulputate ut imperdiet non, suscipit sit amet eros.
      |Pellentesque quam velit, sodales quis congue in, interdum a tortor. In sed enim faucibus, laoreet elit ut, efficitur metus. Sed at pretium leo. Proin aliquet eget justo vel rutrum. Vivamus dictum neque libero, eget ornare purus tincidunt egestas. Aliquam vehicula hendrerit lectus, vitae tincidunt eros. Aliquam erat volutpat. Mauris massa leo, gravida a dignissim ut, maximus sit amet orci. Proin eu pellentesque leo. Aliquam sollicitudin orci commodo metus vehicula interdum. Praesent sodales nec turpis quis sagittis. Sed scelerisque sed nibh non mollis.
      |Sed sit amet vulputate enim. In erat neque, elementum a urna eu, consectetur pulvinar massa. Nunc magna arcu, mollis at dictum vitae, feugiat id mi. Fusce eu lorem mauris. Pellentesque laoreet euismod lacus eget eleifend. Vivamus justo nisi, consectetur euismod tempus eget, suscipit a arcu. Suspendisse potenti. Integer eu posuere mauris, consectetur ornare sapien. Praesent molestie, nunc ut gravida varius, lectus nunc consequat purus, eu fringilla libero risus sed odio. Aliquam eu lorem vitae ex congue posuere. Phasellus vitae consectetur neque, ut dictum ligula.
      |Donec vel ullamcorper lorem, volutpat euismod tortor. Curabitur nec eros augue. Nunc iaculis mattis lacus. Maecenas non tellus ac velit bibendum porttitor. Vivamus eu leo vel est ultricies feugiat vehicula et urna. Duis egestas, ipsum non viverra lacinia, nulla ante malesuada dui, et tempor sem est ac nisi. Nam sed ex et purus varius efficitur in quis est. Curabitur fringilla dictum tellus in tincidunt. Sed neque mauris, laoreet id ultrices eget, consectetur ut metus. Proin interdum efficitur consequat. Sed vulputate nibh eget mollis gravida.
      |Interdum et malesuada fames ac ante ipsum primis in faucibus. In hac habitasse platea dictumst. Nulla id molestie nibh. Maecenas laoreet consequat enim vitae volutpat. Proin efficitur eros ac pharetra posuere. Fusce eros enim, malesuada in orci non, imperdiet scelerisque orci. Fusce feugiat nibh id elit dignissim volutpat. Nam non purus elementum, sollicitudin augue a, ornare diam. In congue efficitur est, a porttitor mauris dignissim a.
      |""".stripMargin

  private val defaultStyle =
    ParagraphStyle.empty.copy(
      fontFamily = Some("Times"),
      fontWeight = Some(400),
      fontStyle = Some("normal"),
      fontSize = Some(16)
    )

  def flowFromString(s: String): Flow =
    Flow(
      s.split("\n").map(paraFromString).toList
    )

  def paraFromString(s: String): Paragraph =
    Paragraph(List(Span(s.map(Character.apply).toList, CharacterStyle.empty)), defaultStyle)

}
