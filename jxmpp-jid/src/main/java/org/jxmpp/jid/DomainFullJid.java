/**
 *
 * Copyright © 2014-2016 Florian Schmaus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jxmpp.jid;

/**
 * An XMPP address (JID) consisting of a domainpart and a resourcepart. For example
 * "xmpp.org/resource".
 * <p>
 * Examples:
 * </p>
 * <ul>
 * <li><code>domain.part/resource</code></li>
 * <li><code>example.net/8c6def89</code></li>
 * </ul>
 *
 * @see Jid
 */
public interface DomainFullJid extends Jid, FullJid, DomainJid {

}
