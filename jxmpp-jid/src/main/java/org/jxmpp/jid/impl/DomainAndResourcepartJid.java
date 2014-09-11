/**
 *
 * Copyright © 2014 Florian Schmaus
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
package org.jxmpp.jid.impl;

import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.DomainFullJid;
import org.jxmpp.stringprep.XmppStringPrepUtil;
import org.jxmpp.stringprep.XmppStringprepException;
import org.jxmpp.util.XmppStringUtils;

/**
 * RFC6122 2.4 allows JIDs with only a domain and resource part.
 * <p>
 * Note that this implementation does not require an cache for the unescaped
 * string, compared to {@link LocalDomainAndResourcepartJid}.
 * </p>
 *
 */
public class DomainAndResourcepartJid extends DomainpartJid implements DomainFullJid {

	private final String resource;

	private String cache;
	private DomainBareJid domainBareJidCache;

	DomainAndResourcepartJid(String domain, String resource) throws XmppStringprepException {
		super(domain);
		resource = XmppStringPrepUtil.resourceprep(resource);
		assertNotLongerThen1023BytesOrEmpty(resource);
		this.resource = resource;
	}

	@Override
	public final String getResource() {
		return resource;
	}

	@Override
	public String toString() {
		if (cache != null) {
			return cache;
		}
		cache = super.toString() + '/' + resource;
		return cache;
	}

	@Override
	public DomainBareJid asDomainBareJid() {
		if (domainBareJidCache == null) {
			try {
				domainBareJidCache = JidCreate.domainBareFrom(XmppStringUtils
						.completeJidFrom(null, domain, resource));
			} catch (XmppStringprepException e) {
				throw new AssertionError(e);
			}
		}
		return domainBareJidCache;
	}

	@Override
	public final boolean hasNoResource() {
		return false;
	}
}
