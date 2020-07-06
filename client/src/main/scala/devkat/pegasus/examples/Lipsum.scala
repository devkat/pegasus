package devkat.pegasus.examples

import devkat.pegasus.model.{Element, Flow, Paragraph, Section}

object Lipsum {

  val lipsum: String =
    """Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent varius viverra suscipit. Quisque tristique, dui ac pretium sagittis, metus odio condimentum metus, et accumsan purus tortor non mi. Proin iaculis tristique malesuada. Pellentesque ac justo non leo venenatis pharetra. Proin dictum neque eros, quis sagittis dolor venenatis facilisis. Aliquam pulvinar eros dolor, vel pellentesque nunc tristique et. Integer ac nunc placerat lectus aliquet pellentesque ac eu risus. Quisque suscipit urna lacinia purus vehicula, at rutrum neque vulputate. Curabitur placerat eleifend quam quis laoreet. Pellentesque id placerat justo, consectetur pharetra enim. Integer leo sapien, vulputate ut imperdiet non, suscipit sit amet eros.
      |Pellentesque quam velit, sodales quis congue in, interdum a tortor. In sed enim faucibus, laoreet elit ut, efficitur metus. Sed at pretium leo. Proin aliquet eget justo vel rutrum. Vivamus dictum neque libero, eget ornare purus tincidunt egestas. Aliquam vehicula hendrerit lectus, vitae tincidunt eros. Aliquam erat volutpat. Mauris massa leo, gravida a dignissim ut, maximus sit amet orci. Proin eu pellentesque leo. Aliquam sollicitudin orci commodo metus vehicula interdum. Praesent sodales nec turpis quis sagittis. Sed scelerisque sed nibh non mollis.
      |Sed sit amet vulputate enim. In erat neque, elementum a urna eu, consectetur pulvinar massa. Nunc magna arcu, mollis at dictum vitae, feugiat id mi. Fusce eu lorem mauris. Pellentesque laoreet euismod lacus eget eleifend. Vivamus justo nisi, consectetur euismod tempus eget, suscipit a arcu. Suspendisse potenti. Integer eu posuere mauris, consectetur ornare sapien. Praesent molestie, nunc ut gravida varius, lectus nunc consequat purus, eu fringilla libero risus sed odio. Aliquam eu lorem vitae ex congue posuere. Phasellus vitae consectetur neque, ut dictum ligula.
      |Donec vel ullamcorper lorem, volutpat euismod tortor. Curabitur nec eros augue. Nunc iaculis mattis lacus. Maecenas non tellus ac velit bibendum porttitor. Vivamus eu leo vel est ultricies feugiat vehicula et urna. Duis egestas, ipsum non viverra lacinia, nulla ante malesuada dui, et tempor sem est ac nisi. Nam sed ex et purus varius efficitur in quis est. Curabitur fringilla dictum tellus in tincidunt. Sed neque mauris, laoreet id ultrices eget, consectetur ut metus. Proin interdum efficitur consequat. Sed vulputate nibh eget mollis gravida.
      |Interdum et malesuada fames ac ante ipsum primis in faucibus. In hac habitasse platea dictumst. Nulla id molestie nibh. Maecenas laoreet consequat enim vitae volutpat. Proin efficitur eros ac pharetra posuere. Fusce eros enim, malesuada in orci non, imperdiet scelerisque orci. Fusce feugiat nibh id elit dignissim volutpat. Nam non purus elementum, sollicitudin augue a, ornare diam. In congue efficitur est, a porttitor mauris dignissim a.
      |""".stripMargin

  def flowFromString(s: String): Flow =
    Flow(
      Vector(
        Section(
          s.split("\n").map(paraFromString).toVector
        )
      )
    )

  def paraFromString(s: String): Paragraph =
    Paragraph(s.map(Element.Character.apply).toVector)

}
